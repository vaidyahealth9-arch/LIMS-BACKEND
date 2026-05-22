package com.halo.lims.security;

import lombok.extern.slf4j.Slf4j;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * HMAC-SHA256 request signing utility.
 * 
 * Used for secure service-to-service communication.
 * Both client and server use shared INTERNAL_SECRET_KEY to sign/verify requests.
 */
@Slf4j
public class RequestSigningUtil {
    
    private static final String ALGORITHM = "HmacSHA256";
    
    /**
     * Sign a request message with HMAC-SHA256
     * 
     * @param message Request body or canonical representation
     * @param secretKey Shared secret (INTERNAL_SECRET_KEY)
     * @return Base64-encoded signature
     */
    public static String sign(String message, String secretKey) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8),
                0,
                secretKey.getBytes(StandardCharsets.UTF_8).length,
                ALGORITHM
            );
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            log.error("Error signing request", e);
            throw new RuntimeException("Failed to sign request", e);
        }
    }
    
    /**
     * Verify a request signature
     * 
     * @param message Request body or canonical representation
     * @param signature Base64-encoded signature from header
     * @param secretKey Shared secret (INTERNAL_SECRET_KEY)
     * @return true if signature matches, false otherwise
     */
    public static boolean verify(String message, String signature, String secretKey) {
        String expectedSignature = sign(message, secretKey);
        return expectedSignature.equals(signature);
    }
    
    /**
     * Create canonical message for signing
     * Format: METHOD:PATH:TIMESTAMP:BODY
     */
    public static String createCanonicalMessage(String method, String path, String timestamp, String body) {
        return String.format("%s:%s:%s:%s", method, path, timestamp, body != null ? body : "");
    }
}
