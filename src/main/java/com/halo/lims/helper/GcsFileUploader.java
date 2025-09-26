package com.halo.lims.helper;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility for uploading files to Google Cloud Storage.
 */
@Component
public class GcsFileUploader {

    @Value("${gcp.project.id:local-gcp-project}")
    private String gcpProjectId;

    /** todo below is GCP
    private final Storage storage;*/

    public GcsFileUploader() {
        // Initialize GCS client. This will use Application Default Credentials.
        // Ensure your service account has "Storage Object Admin" or "Storage Object Creator" role.
        System.out.println("GCS File Uploader (local development mode): Not initializing GCP Storage client.");
        /** todo below is GCP
        this.storage = StorageOptions.getDefaultInstance().getService();*/
    }

    /**
     * Uploads a file from a local path to a specified GCS bucket.
     * @param bucketName The name of the GCS bucket.
     * @param objectName The name of the object (file) in the bucket.
     * @param filePath The local path to the file to upload.
     * @return The public URL of the uploaded object (if public access is enabled), otherwise a GCS path.
     * @throws IOException If an I/O error occurs.
     */
    public String uploadFile(String bucketName, String objectName, Path filePath, String contentType) throws IOException {

        System.out.println("GCS File Uploader (local development mode): Simulating upload of file " + filePath.getFileName() + " to " + bucketName + "/" + objectName);
        return String.format("gs://%s/%s", bucketName, objectName);

        /** todo below is GCP
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();

        storage.create(blobInfo, Files.readAllBytes(filePath));

        System.out.println("File " + filePath + " uploaded to bucket " + bucketName + " as " + objectName);
        return String.format("gs://%s/%s", bucketName, objectName);*/
    }

    /**
     * Uploads a file from an InputStream to a specified GCS bucket.
     * Useful for uploading directly from memory or a network stream.
     * @param bucketName The name of the GCS bucket.
     * @param objectName The name of the object (file) in the bucket.
     * @param inputStream The InputStream of the file content.
     * @param contentType The MIME type of the content (e.g., "application/pdf", "image/dicom").
     * @return The public URL of the uploaded object (if public access is enabled), otherwise a GCS path.
     * @throws IOException If an I/O error occurs.
     */
    public String uploadFile(String bucketName, String objectName, InputStream inputStream, String contentType) throws IOException {

        System.out.println("GCS File Uploader (local development mode): Simulating upload of InputStream to " + bucketName + "/" + objectName);

        if (inputStream != null) {
            inputStream.close();
        }
        return String.format("gs://%s/%s", bucketName, objectName);

        /** todo below is GCP
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();

        storage.createFrom(blobInfo, inputStream); // Use createFrom for InputStream

        System.out.println("Input stream uploaded to bucket " + bucketName + " as " + objectName);
        return String.format("gs://%s/%s", bucketName, objectName);*/
    }
}