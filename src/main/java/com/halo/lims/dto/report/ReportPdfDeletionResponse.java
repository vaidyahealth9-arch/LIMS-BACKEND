package com.halo.lims.dto.report;

public record ReportPdfDeletionResponse(
        Integer serviceRequestId,
        boolean deleted,
        String deletedStorageReference,
        String message
) {}