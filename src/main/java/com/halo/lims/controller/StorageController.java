package com.halo.lims.controller;

import com.halo.lims.repository.MediaAssetRepository;
import com.halo.lims.model.MediaAsset;
import com.halo.lims.service.GcsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {

    private final GcsService gcsService;
    private final MediaAssetRepository mediaAssetRepository;

    private static String limit(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Received empty file"));
        }

        try {
            log.info("Received upload request for file: {}, size: {}", file.getOriginalFilename(), file.getSize());

            String safeFileName = (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank())
                ? ("upload_" + System.currentTimeMillis())
                : file.getOriginalFilename();
            String safeMimeType = (file.getContentType() == null || file.getContentType().isBlank())
                ? "application/octet-stream"
                : file.getContentType();
            
            // Determine folder based on filename or other context if needed
            String folder = "uploads/";
            
            String url = gcsService.uploadFile(
                    file.getBytes(), 
                safeMimeType,
                    folder, 
                safeFileName
            );

            boolean inlineStorage = url != null && url.startsWith("data:");
            String safeStoragePath = inlineStorage ? ("inline/" + safeFileName) : (folder + safeFileName);
            String safePublicUrl = inlineStorage ? null : limit(url, 1024);

            // Record metadata in MediaAsset table
            MediaAsset asset = MediaAsset.builder()
                    .fileName(safeFileName)
                    .assetType("STORAGE_UPLOAD")
                    .entityType("SYSTEM_UPLOAD")
                    .entityId(0)
                    .url(url)
                    .publicUrl(safePublicUrl)
                    .mimeType(safeMimeType)
                    .fileSize(file.getSize())
                    .fileSizeBytes((int) Math.min(Integer.MAX_VALUE, file.getSize()))
                    .storageProvider(inlineStorage ? "INLINE" : "GCS")
                    .storagePath(limit(safeStoragePath, 1024))
                    .build();
            
            MediaAsset savedAsset = mediaAssetRepository.save(asset);

            return ResponseEntity.ok(Map.of(
                    "url", url,
                    "assetId", savedAsset.getId()
            ));
        } catch (IOException e) {
            log.error("Failed to read file bytes", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to process file"));
        } catch (Exception e) {
            log.error("File upload failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }
}
