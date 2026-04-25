package com.halo.lims.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.io.IOException;

@Service
@ConditionalOnProperty(name = "app.report.storage.provider", havingValue = "gcs")
public class GcsStorageService implements ReportStorageService {

    private final Storage storage;
    private final String bucketName;

    public GcsStorageService(Storage storage, @Value("${spring.cloud.gcp.storage.bucket-name}") String bucketName) {
        this.storage = storage;
        this.bucketName = bucketName;
    }

    /**
     * Uploads a file to Google Cloud Storage.
     *
     * @param objectName The name for the object in GCS (e.g., "reports/report-123.pdf").
     * @param inputStream The InputStream of the file to upload.
     * @return The public URL of the uploaded object.
     * @throws IOException if the upload fails.
     */
    public String uploadFile(String objectName, InputStream inputStream) throws IOException {
        return uploadFile(objectName, inputStream.readAllBytes(), "application/octet-stream");
    }

    @Override
    public String uploadFile(String objectName, byte[] content, String contentType) {
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();

        storage.create(blobInfo, content);

        return String.format("gs://%s/%s", bucketName, objectName);
    }

    @Override
    public byte[] downloadFile(String storedReference) {
        String objectName = resolveObjectName(storedReference);
        Blob blob = storage.get(BlobId.of(bucketName, objectName));
        if (blob == null) {
            throw new IllegalStateException("GCS object not found: " + storedReference);
        }
        return blob.getContent();
    }

    @Override
    public void deleteFile(String storedReference) {
        String objectName = resolveObjectName(storedReference);
        storage.delete(BlobId.of(bucketName, objectName));
    }

    private String resolveObjectName(String storedReference) {
        if (storedReference == null || storedReference.isBlank()) {
            throw new IllegalArgumentException("Stored reference must not be blank");
        }

        String value = storedReference.trim();
        if (value.startsWith("gs://")) {
            String withoutScheme = value.substring("gs://".length());
            int firstSlash = withoutScheme.indexOf('/');
            return firstSlash >= 0 ? withoutScheme.substring(firstSlash + 1) : withoutScheme;
        }

        if (value.startsWith("https://storage.googleapis.com/")) {
            String withoutPrefix = value.substring("https://storage.googleapis.com/".length());
            int firstSlash = withoutPrefix.indexOf('/');
            return firstSlash >= 0 ? withoutPrefix.substring(firstSlash + 1) : withoutPrefix;
        }

        if (value.startsWith("https://storage.cloud.google.com/")) {
            String withoutPrefix = value.substring("https://storage.cloud.google.com/".length());
            int firstSlash = withoutPrefix.indexOf('/');
            return firstSlash >= 0 ? withoutPrefix.substring(firstSlash + 1) : withoutPrefix;
        }

        return value.startsWith("/") ? value.substring(1) : value;
    }
}
