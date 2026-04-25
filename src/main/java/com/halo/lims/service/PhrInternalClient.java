package com.halo.lims.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;
import com.halo.lims.dto.patient.PatientRegistrationResponse;
import com.halo.lims.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PhrInternalClient {

    private static final Logger log = LoggerFactory.getLogger(PhrInternalClient.class);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String internalAuthMode;
    private final String environment;
    private final String internalSecretKey;
    private final String phrServiceUrl;
    private final String phrServiceUrlCloud;

    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private volatile Instant circuitOpenUntil = Instant.EPOCH;

    public PhrInternalClient(
            ObjectMapper objectMapper,
            @Value("${app.internal.auth.mode:auto}") String internalAuthMode,
            @Value("${app.environment:development}") String environment,
            @Value("${app.internal.secret-key:}") String internalSecretKey,
            @Value("${app.internal.phr-base-url:http://localhost:8000}") String phrServiceUrl,
            @Value("${app.internal.phr-base-url-cloud:}") String phrServiceUrlCloud
    ) {
        this.objectMapper = objectMapper;
        this.internalAuthMode = internalAuthMode;
        this.environment = environment;
        this.internalSecretKey = internalSecretKey;
        this.phrServiceUrl = phrServiceUrl;
        this.phrServiceUrlCloud = phrServiceUrlCloud;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public java.util.Optional<PatientRegistrationResponse> fetchPatientProfileByMobile(String mobile, String relationship) {
        String normalizedMobile = Patient.normalizePhone(mobile);
        if (normalizedMobile == null) {
            return java.util.Optional.empty();
        }

        if (isCircuitOpen()) {
            log.warn("PHR internal client circuit open; skipping lookup for mobile ending {}", last4(normalizedMobile));
            return java.util.Optional.empty();
        }

        String encodedMobile = URLEncoder.encode(normalizedMobile, StandardCharsets.UTF_8);
        String url = resolveBaseUrl() + "/api/v1/auth/users/by-phone/" + encodedMobile;
        if (relationship != null && !relationship.isBlank()) {
            url += "?relationship=" + URLEncoder.encode(relationship.toLowerCase(), StandardCharsets.UTF_8);
        }

        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                log.info("Calling PHR lookup attempt={} mobileEnding={} url={}", attempt + 1, last4(normalizedMobile), url);
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .header("Accept", "application/json")
                        .header("X-User-Mobile", normalizedMobile)
                        .GET();

                if (isOidcAuthEnabled()) {
                    requestBuilder.header("Authorization", "Bearer " + fetchIdToken(resolveBaseUrl()));
                } else {
                    if (internalSecretKey == null || internalSecretKey.isBlank()) {
                        throw new IllegalStateException("INTERNAL_SECRET_KEY is required for local internal auth");
                    }
                    requestBuilder.header("X-Internal-Secret", internalSecretKey);
                }

                HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();
                log.info("PHR lookup response status={} mobileEnding={}", status, last4(normalizedMobile));

                if (status == HttpStatus.OK.value()) {
                    markSuccess();
                    PatientRegistrationResponse mapped = mapPatientRegistrationResponse(response.body());
                    log.info("PHR lookup mapped successfully mobileEnding={} id={}", last4(normalizedMobile), mapped.getId());
                    return java.util.Optional.of(mapped);
                }

                if (status == HttpStatus.NOT_FOUND.value()) {
                    markSuccess();
                    return java.util.Optional.empty();
                }

                if (isRetryableStatus(status) && attempt < 2) {
                    markFailure();
                    sleepBackoff(attempt);
                    continue;
                }

                markFailure();
                log.warn("PHR lookup failed with status {} for mobile ending {}", status, last4(normalizedMobile));
                return java.util.Optional.empty();
            } catch (Exception ex) {
                markFailure();
                if (attempt < 2) {
                    log.warn("PHR lookup attempt {} failed for mobile ending {}: {}", attempt + 1, last4(normalizedMobile), ex.toString());
                    sleepBackoff(attempt);
                    continue;
                }
                log.warn("PHR lookup failed for mobile ending {}: {}", last4(normalizedMobile), ex.toString());
                return java.util.Optional.empty();
            }
        }

        return java.util.Optional.empty();
    }

    private PatientRegistrationResponse mapPatientRegistrationResponse(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        PatientRegistrationResponse response = new PatientRegistrationResponse();
        response.setId(readInt(root, "id"));
        response.setFirstName(readText(root, "first_name", "firstName"));
        response.setLastName(readText(root, "last_name", "lastName"));
        response.setGender(readText(root, "gender"));
        response.setDateOfBirth(readDate(root, "date_of_birth", "dateOfBirth"));
        response.setContactPhone(readText(root, "contact_phone", "contactPhone"));
        response.setContactEmail(readText(root, "contact_email", "contactEmail"));
        response.setAddressLine1(readText(root, "address_line1", "addressLine1"));
        response.setCity(readText(root, "city"));
        response.setState(readText(root, "state"));
        response.setPostalCode(readText(root, "postal_code", "postalCode"));
        return response;
    }

    private Integer readInt(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        return node != null && node.isNumber() ? node.intValue() : null;
    }

    private String readText(JsonNode root, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode node = root.get(fieldName);
            if (node != null && !node.isNull()) {
                String value = node.asText();
                if (!value.isBlank()) {
                    return value;
                }
            }
        }
        return null;
    }

    private LocalDate readDate(JsonNode root, String... fieldNames) {
        String value = readText(root, fieldNames);
        if (value == null) {
            return null;
        }

        try {
            return LocalDate.parse(value.length() >= 10 ? value.substring(0, 10) : value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String resolveBaseUrl() {
        if (isOidcAuthEnabled() && phrServiceUrlCloud != null && !phrServiceUrlCloud.isBlank()) {
            return trimTrailingSlash(phrServiceUrlCloud);
        }
        return trimTrailingSlash(phrServiceUrl);
    }

    private boolean isOidcAuthEnabled() {
        String mode = internalAuthMode == null ? "auto" : internalAuthMode.trim().toLowerCase();
        String env = environment == null ? "development" : environment.trim().toLowerCase();

        if ("oidc".equals(mode)) {
            return true;
        }
        if ("local".equals(mode)) {
            return false;
        }
        return "prod".equals(env) || "production".equals(env);
    }

    private String fetchIdToken(String audience) throws Exception {
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        if (!(credentials instanceof IdTokenProvider idTokenProvider)) {
            throw new IllegalStateException("Runtime credentials do not support ID tokens");
        }

        IdTokenCredentials tokenCredentials = IdTokenCredentials.newBuilder()
                .setIdTokenProvider(idTokenProvider)
                .setTargetAudience(audience)
                .build();

        tokenCredentials.refreshIfExpired();
        if (tokenCredentials.getAccessToken() == null) {
            tokenCredentials.refresh();
        }
        return tokenCredentials.getAccessToken().getTokenValue();
    }

    private boolean isRetryableStatus(int status) {
        return status == 408 || status == 429 || status >= 500;
    }

    private void sleepBackoff(int attempt) {
        long delayMs = (long) (300 * Math.pow(2, attempt)) + ThreadLocalRandom.current().nextLong(150);
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isCircuitOpen() {
        return Instant.now().isBefore(circuitOpenUntil);
    }

    private void markSuccess() {
        consecutiveFailures.set(0);
        circuitOpenUntil = Instant.EPOCH;
    }

    private void markFailure() {
        int failures = consecutiveFailures.incrementAndGet();
        if (failures >= 5) {
            circuitOpenUntil = Instant.now().plusSeconds(30);
        }
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String last4(String normalizedMobile) {
        if (normalizedMobile == null || normalizedMobile.length() <= 4) {
            return normalizedMobile;
        }
        return normalizedMobile.substring(normalizedMobile.length() - 4);
    }
}
