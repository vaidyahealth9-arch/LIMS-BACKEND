package com.halo.lims.security;

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import jakarta.annotation.PostConstruct;
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
 * Utility for AES-256 GCM encryption and decryption.
 * The encryption key is retrieved from Google Secret Manager.
 */
@Component
public class AesGcmEncryptionUtil {

    // AES-GCM parameters
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 16; // 128 bits
    private static final int AES_KEY_SIZE = 256; // 256 bits

    private SecretKey secretKey;

    @Value("${gcp.project.id}")
    private String gcpProjectId;

    @Value("${encryption.secret-manager.key-name}")
    private String encryptionKeySecretName;

    // Public constructor for testing, or if key management is handled externally
    public AesGcmEncryptionUtil() {
        // For Spring to inject values, then init() will be called.
        // Or for testing with a mock key.
    }

    // Initialize the encryption key from Secret Manager after properties are injected
    @PostConstruct
    public void init() {
        System.out.println("AES GCM Encryption utility (local development mode): Not connecting to Secret Manager.");
        // --- START OF LOCAL DEVELOPMENT BYPASS ---
        try {
            // For local development, create a dummy key or just bypass encryption.
            // Here, we'll create a dummy key to prevent NPEs in cipher operations
            // but the encrypt/decrypt methods below will still just pass through.
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(AES_KEY_SIZE, SecureRandom.getInstanceStrong());
            this.secretKey = keyGen.generateKey(); // Dummy key
            System.out.println("Dummy AES key generated for local encryption utility.");
        } catch (Exception e) {
            System.err.println("Failed to generate dummy AES key: " + e.getMessage());
        }
        /** todo below is GCP
        try {
            this.secretKey = getSecretKeyFromSecretManager(gcpProjectId, encryptionKeySecretName);
            // Self-test for encryption utility on startup
            String testString = "test-string-for-encryption-check";
            String encrypted = encrypt(testString);
            String decrypted = decrypt(encrypted);
            if (!testString.equals(decrypted)) {
                throw new IllegalStateException("AES GCM Encryption/Decryption self-test failed on startup!");
            }
            System.out.println("AES GCM Encryption utility initialized successfully and passed self-test.");
        } catch (Exception e) {
            System.err.println("Failed to initialize AES GCM Encryption utility: " + e.getMessage());
            // Depending on criticality, you might want to throw RuntimeException here to halt application startup
            throw new RuntimeException("Encryption utility initialization failed.", e);
        }*/
    }

    /** todo below is GCP
    private SecretKey getSecretKeyFromSecretManager(String projectId, String secretName) throws Exception {
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretName, "latest");
            String secretValue = client.accessSecretVersion(secretVersionName).getPayload().getData().toStringUtf8();
            // The secretValue should be the Base64 encoded raw AES key (256-bit / 32 bytes)
            byte[] keyBytes = Base64.getDecoder().decode(secretValue);
            if (keyBytes.length * 8 != AES_KEY_SIZE) {
                throw new IllegalArgumentException("Retrieved secret key size mismatch. Expected " + AES_KEY_SIZE + " bits, got " + (keyBytes.length * 8) + " bits.");
            }
            return new SecretKeySpec(keyBytes, "AES");
        }
    }*/

    /**
     * Generates a new 256-bit AES key. This should be run once to generate the key,
     * which then should be securely stored in Google Secret Manager.
     * @return Base64 encoded AES key string.
     */
    public static String generateNewAesKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE, SecureRandom.getInstanceStrong());
        SecretKey secretKey = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        return plainText;

        /** todo below is GCP
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv); // Generate a new IV for each encryption

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv));

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Error while encrypting data", e);
        }*/
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        return encryptedText;
        /** todo below is GCP
        try {
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
        } catch (Exception e) {
            // Log the error but return null or re-throw a specific decryption exception
            // depending on how you want to handle decryption failures for non-critical data.
            // For PII, a failure is critical.
            throw new RuntimeException("Error while decrypting data", e);
        }*/
    }

    /*public static void main(String[] args) throws Exception {
        String newKey = AesGcmEncryptionUtil.generateNewAesKey();
        System.out.println("Generated AES-256 Key (Base64 encoded): " + newKey);
        // STORE THIS KEY SECURELY IN GOOGLE SECRET MANAGER! k/HA5Xz3vxbCF9mr3Th7dRQYBm+TChOpNeBIO7Lm/kc=
    }*/
}
