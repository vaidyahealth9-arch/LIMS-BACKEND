package com.halo.lims.security;

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256 GCM encryption/decryption utility for PII fields at rest.
 *
 * <p>In production (GCP Secret Manager enabled), the encryption key is fetched from
 * Secret Manager on startup. A self-test is run to verify correctness.
 *
 * <p>In local development ({@code spring.cloud.gcp.secretmanager.enabled=false}),
 * a random in-memory key is generated per-session — data encrypted locally is NOT
 * persistent across restarts and should never be used for real patient data.
 */
@Component
public class AesGcmEncryptionUtil {

    private static final Logger log = LoggerFactory.getLogger(AesGcmEncryptionUtil.class);

    private static final int GCM_IV_LENGTH = 12;   // 96 bits
    private static final int GCM_TAG_LENGTH = 16;  // 128 bits
    private static final int AES_KEY_SIZE = 256;   // 256 bits

    private SecretKey secretKey;
    private boolean encryptionActive = false;

    @Value("${spring.cloud.gcp.project-id:}")
    private String gcpProjectId;

    @Value("${encryption.secret-manager.key-name:lims-aes-256-key}")
    private String encryptionKeySecretName;

    @Value("${spring.cloud.gcp.secretmanager.enabled:false}")
    private boolean secretManagerEnabled;

    public AesGcmEncryptionUtil() {
        // Spring will inject @Value fields, then call @PostConstruct init().
    }

    @PostConstruct
    public void init() {
        if (secretManagerEnabled && gcpProjectId != null && !gcpProjectId.isBlank()) {
            try {
                this.secretKey = getSecretKeyFromSecretManager(gcpProjectId, encryptionKeySecretName);
                // Self-test encryption roundtrip on startup
                String testString = "lims-aes-selftest";
                String encrypted = encryptInternal(testString);
                String decrypted = decryptInternal(encrypted);
                if (!testString.equals(decrypted)) {
                    throw new IllegalStateException("AES GCM self-test FAILED: encrypt/decrypt roundtrip mismatch.");
                }
                this.encryptionActive = true;
                log.info("AES-GCM encryption initialized successfully via GCP Secret Manager (project={}, key={}).",
                        gcpProjectId, encryptionKeySecretName);
            } catch (Exception e) {
                log.error("FATAL: Failed to initialize AES-GCM encryption from Secret Manager: {}", e.getMessage(), e);
                throw new RuntimeException("Encryption utility initialization failed. Cannot start without a valid encryption key.", e);
            }
        } else {
            // Local development — use a fixed static key.
            // Data encrypted with this key is persistent locally but NOT secure.
            try {
                byte[] staticKey = "local-dev-secret-key-12345678901".getBytes(StandardCharsets.UTF_8);
                this.secretKey = new SecretKeySpec(staticKey, "AES");
                this.encryptionActive = true;
                log.warn("AES-GCM encryption running in LOCAL mode with a fixed static key. " +
                        "Data will survive restarts locally. DO NOT use this key in production.");
            } catch (Exception e) {
                log.error("Failed to set local AES key: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to initialize local encryption key.", e);
            }
        }
    }

    private SecretKey getSecretKeyFromSecretManager(String projectId, String secretName) throws Exception {
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretName, "latest");
            String secretValue = client.accessSecretVersion(secretVersionName)
                    .getPayload().getData().toStringUtf8();
            byte[] keyBytes = Base64.getDecoder().decode(secretValue);
            if (keyBytes.length * 8 != AES_KEY_SIZE) {
                throw new IllegalArgumentException(
                        "Secret key size mismatch. Expected " + AES_KEY_SIZE + " bits, got " + (keyBytes.length * 8) + " bits.");
            }
            return new SecretKeySpec(keyBytes, "AES");
        }
    }

    /**
     * Encrypts a plaintext string using AES-256-GCM.
     * Returns the Base64-encoded ciphertext (IV prepended).
     * Returns {@code null} if input is {@code null}.
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            return encryptInternal(plainText);
        } catch (Exception e) {
            throw new RuntimeException("AES-GCM encryption failed.", e);
        }
    }

    /**
     * Decrypts a Base64-encoded AES-256-GCM ciphertext string.
     * Returns {@code null} if input is {@code null}.
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        try {
            return decryptInternal(encryptedText);
        } catch (Exception e) {
            log.warn("AES-GCM decryption failed for a value (returning null as fallback). " +
                     "This usually happens if the data was unencrypted or encrypted with a previous session key. Error: {}", e.getMessage());
            // Prevent returning the raw encrypted Base64 string to the frontend to avoid garbage data and potential PII leak
            return null;
        }
    }

    private String encryptInternal(String plainText) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv));

        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
        byteBuffer.put(iv);
        byteBuffer.put(cipherText);
        return Base64.getEncoder().encodeToString(byteBuffer.array());
    }

    private String decryptInternal(String encryptedText) throws Exception {
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);

        ByteBuffer byteBuffer = ByteBuffer.wrap(decodedBytes);
        byte[] iv = new byte[GCM_IV_LENGTH];
        byteBuffer.get(iv);

        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv));

        byte[] plainTextBytes = cipher.doFinal(cipherText);
        return new String(plainTextBytes, StandardCharsets.UTF_8);
    }

    /**
     * Utility: generates a new random AES-256 key and prints it to stdout.
     * Run once to create a key, then store it in GCP Secret Manager.
     * <pre>
     *   java -cp target/lims-*.jar com.halo.lims.security.AesGcmEncryptionUtil
     * </pre>
     */
    public static void main(String[] args) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE, SecureRandom.getInstanceStrong());
        String newKey = Base64.getEncoder().encodeToString(keyGen.generateKey().getEncoded());
        System.out.println("Generated AES-256 Key (Base64). Store securely in GCP Secret Manager:");
        System.out.println(newKey);
    }
}
