package com.halo.lims.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.UUID;

@Service
public class GcsService {

    private static final Logger log = LoggerFactory.getLogger(GcsService.class);

    private final Storage storage;
    private final String bucketName;
    private final boolean localInlineFallbackEnabled;

    public GcsService(ObjectProvider<Storage> storageProvider,
                      @Value("${spring.cloud.gcp.storage.bucket-name:}") String bucketName,
                      @Value("${app.media.storage.local-inline-fallback:true}") boolean localInlineFallbackEnabled) {
        this.storage = storageProvider.getIfAvailable();
        this.bucketName = bucketName;
        this.localInlineFallbackEnabled = localInlineFallbackEnabled;
    }

    /**
     * Uploads a base64 encoded image to GCS and returns the public URL.
     * @param dataUri The base64 data URI (e.g. data:image/png;base64,...)
     * @param folder The folder path in the bucket (e.g. "signatures/")
     * @param fileName Optional filename. If null, a UUID will be generated.
     * @return The public URL of the uploaded file.
     */
    public String uploadBase64(String dataUri, String folder, String fileName) {
        if (dataUri == null || dataUri.isBlank()) {
            throw new IllegalArgumentException("Image data must not be blank.");
        }

        String[] parts = dataUri.split(",");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid data URI format.");
        }

        String metadata = parts[0];
        String base64Content = parts[1];
        String mimeType = metadata.substring(metadata.indexOf(":") + 1, metadata.indexOf(";"));
        String extension = mimeType.substring(mimeType.indexOf("/") + 1);

        byte[] content = Base64.getDecoder().decode(base64Content);

        if (fileName == null) {
            fileName = UUID.randomUUID().toString() + "." + extension;
        }

        return uploadToGcs(content, mimeType, folder, fileName);
    }

    /**
     * Uploads a byte array to GCS and returns the public URL.
     * @param content The file content as bytes.
     * @param mimeType The MIME type of the file.
     * @param folder The folder path in the bucket.
     * @param fileName The filename.
     * @return The public URL of the uploaded file.
     */
    public String uploadFile(byte[] content, String mimeType, String folder, String fileName) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("File content must not be blank.");
        }
        
        if (fileName == null) {
            String extension = mimeType != null && mimeType.contains("/") 
                ? mimeType.substring(mimeType.indexOf("/") + 1) : "bin";
            fileName = UUID.randomUUID().toString() + "." + extension;
        }

        return uploadToGcs(content, mimeType, folder, fileName);
    }

    private String uploadToGcs(byte[] content, String mimeType, String folder, String fileName) {
        if (storage == null || bucketName == null || bucketName.isBlank()) {
            if (localInlineFallbackEnabled) {
                log.info("GCS storage not configured. Using inline data URI fallback (if applicable) or failing.");
                // For direct file uploads, there is no real "inline" fallback that makes sense 
                // in the same way as data URIs for small strings. 
                // However, we return a simulated string or re-encode to data URI for compatibility.
                return "data:" + (mimeType != null ? mimeType : "image/png") + ";base64," + Base64.getEncoder().encodeToString(content);
            }
            throw new IllegalStateException("GCP Storage is not configured and local inline fallback is disabled.");
        }

        String objectName = (folder != null ? folder : "") + fileName;
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(mimeType != null ? mimeType : "application/octet-stream")
                .build();

        storage.create(blobInfo, content);

        log.info("Uploaded file to GCS: {}/{}", bucketName, objectName);
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, objectName);
    }
}
