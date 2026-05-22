package com.halo.lims.helper;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility for uploading files directly to Google Cloud Storage.
 *
 * <p>TODO: This class is scaffolded for future GCS integration. Currently it operates
 * in local-simulation mode (returns fake GCS URIs without uploading). Once GCS credentials
 * are configured, replace the simulation blocks with the real GCS SDK calls below each
 * {@code // --- GCS IMPLEMENTATION ---} comment.
 */
@Component
public class GcsFileUploader {

    private static final Logger log = LoggerFactory.getLogger(GcsFileUploader.class);

    @Value("${spring.cloud.gcp.project-id:local-gcp-project}")
    private String gcpProjectId;

    public GcsFileUploader() {
        // GCS client initialization will go here once credentials are in place.
        // this.storage = StorageOptions.newBuilder().setProjectId(gcpProjectId).build().getService();
        log.debug("GcsFileUploader initialized (GCS client not yet active — simulation mode).");
    }

    /**
     * Uploads a file from a local path to a GCS bucket.
     *
     * @param bucketName  The name of the GCS bucket.
     * @param objectName  The destination object name (e.g., "reports/report-123.pdf").
     * @param filePath    The local file to upload.
     * @param contentType The MIME type.
     * @return The GCS URI of the uploaded object.
     * @throws IOException if the file cannot be read.
     */
    public String uploadFile(String bucketName, String objectName, Path filePath, String contentType) throws IOException {
        log.info("GcsFileUploader [SIMULATION]: would upload '{}' to gs://{}/{}", filePath.getFileName(), bucketName, objectName);

        // --- GCS IMPLEMENTATION (activate when credentials are ready) ---
        // BlobId blobId = BlobId.of(bucketName, objectName);
        // BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
        // Storage storage = StorageOptions.getDefaultInstance().getService();
        // storage.create(blobInfo, Files.readAllBytes(filePath));
        // log.info("File '{}' uploaded to gs://{}/{}", filePath.getFileName(), bucketName, objectName);

        return String.format("gs://%s/%s", bucketName, objectName);
    }

    /**
     * Uploads a file from an InputStream to a GCS bucket.
     *
     * @param bucketName  The name of the GCS bucket.
     * @param objectName  The destination object name.
     * @param inputStream The file content stream.
     * @param contentType The MIME type.
     * @return The GCS URI of the uploaded object.
     * @throws IOException if the stream cannot be read.
     */
    public String uploadFile(String bucketName, String objectName, InputStream inputStream, String contentType) throws IOException {
        log.info("GcsFileUploader [SIMULATION]: would upload InputStream to gs://{}/{}", bucketName, objectName);

        if (inputStream != null) {
            inputStream.close();
        }

        // --- GCS IMPLEMENTATION (activate when credentials are ready) ---
        // BlobId blobId = BlobId.of(bucketName, objectName);
        // BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
        // Storage storage = StorageOptions.getDefaultInstance().getService();
        // storage.createFrom(blobInfo, inputStream);
        // log.info("InputStream uploaded to gs://{}/{}", bucketName, objectName);

        return String.format("gs://%s/%s", bucketName, objectName);
    }
}