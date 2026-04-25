package com.halo.lims.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@ConditionalOnProperty(name = "app.report.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalReportStorageService implements ReportStorageService {

    private final Path basePath;

    public LocalReportStorageService(@Value("${app.report.storage.local.base-path:./data/lims-reports}") String basePath) {
        this.basePath = Paths.get(basePath);
    }

    @Override
    public String uploadFile(String objectName, byte[] content, String contentType) {
        try {
            String normalizedObjectName = normalizeObjectName(objectName);
            Path targetFile = basePath.resolve(normalizedObjectName).normalize();
            Files.createDirectories(targetFile.getParent());
            Files.write(targetFile, content);
            return "local://" + normalizedObjectName.replace('\\', '/');
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store report PDF locally", ex);
        }
    }

    @Override
    public byte[] downloadFile(String storedReference) {
        try {
            String normalizedObjectName = normalizeReference(storedReference);
            Path targetFile = basePath.resolve(normalizedObjectName).normalize();
            if (!Files.exists(targetFile)) {
                throw new IllegalStateException("Local report file not found: " + storedReference);
            }
            return Files.readAllBytes(targetFile);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read report PDF from local storage", ex);
        }
    }

    @Override
    public void deleteFile(String storedReference) {
        String normalizedObjectName = normalizeReference(storedReference);
        Path targetFile = basePath.resolve(normalizedObjectName).normalize();

        try {
            Files.deleteIfExists(targetFile);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to delete report PDF from local storage", ex);
        }
    }

    private String normalizeReference(String storedReference) {
        if (storedReference == null || storedReference.isBlank()) {
            throw new IllegalArgumentException("Stored reference must not be blank");
        }

        String value = storedReference.trim();
        if (value.startsWith("local://")) {
            value = value.substring("local://".length());
        }
        return normalizeObjectName(value);
    }

    private String normalizeObjectName(String objectName) {
        String normalized = objectName.replace("\\", "/");
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.contains("..")) {
            throw new IllegalArgumentException("Invalid object name path traversal attempt");
        }
        return normalized;
    }
}
