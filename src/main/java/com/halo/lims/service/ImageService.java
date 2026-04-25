package com.halo.lims.service;

import com.halo.lims.helper.ImageValidationUtil;
import com.halo.lims.model.MediaAsset;
import com.halo.lims.repository.MediaAssetRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ImageService {

    private final ReportImageService reportImageService;
    private final ImageValidationUtil imageValidationUtil;
    private final GcsService gcsService;
    private final MediaAssetRepository mediaAssetRepository;

    private static String limit(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    public ImageService(ReportImageService reportImageService,
                        ImageValidationUtil imageValidationUtil,
                        GcsService gcsService,
                        MediaAssetRepository mediaAssetRepository) {
        this.reportImageService = reportImageService;
        this.imageValidationUtil = imageValidationUtil;
        this.gcsService = gcsService;
        this.mediaAssetRepository = mediaAssetRepository;
    }

    public String buildQrImageUrl(String content) {
        return reportImageService.buildQrImageUrl(content);
    }

    public String buildSparklineSvg(List<BigDecimal> values, int w, int h, BigDecimal refLow, BigDecimal refHigh) {
        return reportImageService.buildSparklineSvg(values, w, h, refLow, refHigh);
    }

    public void validateImageDataUri(String dataUri, String fieldName) {
        imageValidationUtil.validateBase64DataUri(dataUri, fieldName);
    }

    public Integer upsertImageAsset(String dataUri, String assetType, String entityType, Integer entityId, Integer uploadedBy) {
        if (dataUri == null || dataUri.isBlank()) {
            return null;
        }

        String trimmedSource = dataUri.trim();

        // 1. Handle existing URL (already uploaded via /storage/upload flow)
        if (trimmedSource.startsWith("http")) {
            return mediaAssetRepository.findByUrl(trimmedSource)
                    .map(MediaAsset::getId)
                    .orElseGet(() -> {
                        // Create a metadata record for this external/pre-uploaded URL
                        MediaAsset asset = MediaAsset.builder()
                                .fileName(entityType + "_" + entityId + "_" + System.currentTimeMillis())
                                .assetType(assetType)
                            .entityType(entityType == null || entityType.isBlank() ? "UNKNOWN" : entityType)
                            .entityId(entityId == null ? 0 : entityId)
                                .url(trimmedSource)
                            .publicUrl(limit(trimmedSource, 1024))
                            .mimeType("application/octet-stream")
                            .fileSizeBytes(0)
                            .storageProvider("EXTERNAL")
                            .storagePath(limit("external/" + entityType + "/" + (entityId == null ? 0 : entityId), 1024))
                                .uploadedBy(uploadedBy)
                                .build();
                        return mediaAssetRepository.save(asset).getId();
                    });
        }

        // 2. Validate base64 image payload only for legacy data-uri flow
        validateImageDataUri(trimmedSource, assetType == null ? "image" : assetType);

        // 3. Fallback for legacy base64 flow (determine folder and upload)
        String normalizedAssetType = assetType == null ? "MISC" : assetType;
        String folder = switch (normalizedAssetType) {
            case "ORG_HEADER" -> "headers/";
            case "ORG_FOOTER" -> "footers/";
            case "SIGNATURE" -> "signatures/";
            default -> "misc/";
        };

        String fileName = entityType + "_" + entityId + "_" + System.currentTimeMillis();
        String storagePath = folder + fileName;
        String url;
        try {
            url = gcsService.uploadBase64(trimmedSource, folder, fileName);
        } catch (Exception e) {
            // Fallback for local development or if configuration is missing
            System.err.println("GCS Upload failed: " + e.getMessage());
            return null;
        }

        // 4. Record metadata for newly uploaded legacy asset
        MediaAsset asset = MediaAsset.builder()
                .fileName(fileName)
            .assetType(normalizedAssetType)
                .entityType(entityType == null || entityType.isBlank() ? "UNKNOWN" : entityType)
                .entityId(entityId == null ? 0 : entityId)
                .url(url)
                .publicUrl(url != null && url.startsWith("data:") ? null : limit(url, 1024))
                .mimeType("image/*")
                .fileSizeBytes(0)
                .storageProvider(url != null && url.startsWith("data:") ? "INLINE" : "GCS")
                .storagePath(limit(storagePath, 1024))
                .uploadedBy(uploadedBy)
                .build();

        MediaAsset saved = mediaAssetRepository.save(asset);
        return saved.getId();
    }

    public String resolveImageUrl(Integer assetId, String fallbackReference) {
        if (assetId != null) {
            return mediaAssetRepository.findById(assetId)
                    .map(MediaAsset::getUrl)
                    .orElse(fallbackReference != null ? fallbackReference : "");
        }
        return resolveImageUrl(fallbackReference);
    }

    public String resolveImageUrl(String imageReference) {
        if (imageReference == null) {
            return "";
        }
        return imageReference.trim();
    }
}
