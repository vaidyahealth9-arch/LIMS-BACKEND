package com.halo.lims.helper;

import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class ImageValidationUtil {

    public void validateBase64DataUri(String dataUri, String fieldName) {
        if (dataUri == null || dataUri.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        String trimmed = dataUri.trim();
        if (trimmed.startsWith("http")) {
            // Already a public URL, bypass base64 validation
            return;
        }

        if (!trimmed.startsWith("data:")) {
            throw new IllegalArgumentException(fieldName + " must be a base64 data URI or a public URL");
        }

        int commaIndex = trimmed.indexOf(',');
        if (commaIndex < 0) {
            throw new IllegalArgumentException(fieldName + " data URI is missing payload");
        }

        String payload = trimmed.substring(commaIndex + 1);
        if (payload.isBlank()) {
            throw new IllegalArgumentException(fieldName + " data URI payload must not be empty");
        }

        try {
            Base64.getDecoder().decode(payload);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(fieldName + " data URI payload is not valid base64", ex);
        }
    }
}