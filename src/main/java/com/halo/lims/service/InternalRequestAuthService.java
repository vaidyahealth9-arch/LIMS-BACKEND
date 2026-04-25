package com.halo.lims.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.halo.lims.model.Patient;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InternalRequestAuthService {

    private static final Logger log = LoggerFactory.getLogger(InternalRequestAuthService.class);

    private final String internalAuthMode;
    private final String environment;
    private final String internalSecretKey;
    private final String limsAudience;
    private final Set<String> allowedServiceAccounts;

    public InternalRequestAuthService(
            @Value("${app.internal.auth.mode:auto}") String internalAuthMode,
            @Value("${app.environment:development}") String environment,
            @Value("${app.internal.secret-key:}") String internalSecretKey,
            @Value("${app.internal.lims-audience:http://localhost:8080}") String limsAudience,
            @Value("${app.internal.allowed-service-accounts:}") String allowedServiceAccountsRaw
    ) {
        this.internalAuthMode = internalAuthMode;
        this.environment = environment;
        this.internalSecretKey = internalSecretKey;
        this.limsAudience = limsAudience;
        this.allowedServiceAccounts = Arrays.stream(allowedServiceAccountsRaw.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    public void authorizeIntegrationCall(HttpServletRequest request, String requestedMobile) {
        enforcePatientScope(request, requestedMobile);

        if (isOidcAuthEnabled()) {
            authorizeWithOidc(request);
            return;
        }

        authorizeWithSharedSecret(request);
    }

    private void enforcePatientScope(HttpServletRequest request, String requestedMobile) {
        String expectedMobile = Patient.normalizePhone(requestedMobile);
        String callerMobile = Patient.normalizePhone(request.getHeader("X-User-Mobile"));

        if (expectedMobile == null || callerMobile == null || !expectedMobile.equals(callerMobile)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Internal patient-scope validation failed"
            );
        }
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

    private void authorizeWithSharedSecret(HttpServletRequest request) {
        if (internalSecretKey == null || internalSecretKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal shared secret is not configured"
            );
        }

        String providedSecret = request.getHeader("X-Internal-Secret");
        if (providedSecret == null || !safeEquals(internalSecretKey, providedSecret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid internal authentication secret");
        }
    }

    private void authorizeWithOidc(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing bearer token");
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance()
            )
                    .setAudience(List.of(limsAudience))
                    .build();

            GoogleIdToken idToken = verifier.verify(token);
            if (idToken == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid service identity token");
            }

            Object emailObj = idToken.getPayload().get("email");
            String callerEmail = emailObj == null ? "" : emailObj.toString().trim().toLowerCase();
            if (callerEmail.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Service account email missing in token");
            }

            if (!allowedServiceAccounts.isEmpty() && !allowedServiceAccounts.contains(callerEmail)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Service account not allowed");
            }
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Failed to verify internal OIDC token: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid service identity token", ex);
        }
    }

    private boolean safeEquals(String expected, String actual) {
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = actual.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expectedBytes, actualBytes);
    }
}
