package com.halo.lims.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Service
public class ReportImageService {

    private final Cache<String, String> qrCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build();

    public String buildQrImageUrl(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }

        return qrCache.get(content, key -> {
            try {
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                BitMatrix bitMatrix = qrCodeWriter.encode(key, BarcodeFormat.QR_CODE, 120, 120);
                ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
                byte[] pngData = pngOutputStream.toByteArray();
                return "data:image/png;base64," + Base64.getEncoder().encodeToString(pngData);
            } catch (Exception e) {
                return "";
            }
        });
    }

    public String buildSparklineSvg(List<BigDecimal> values, int w, int h, BigDecimal refLow, BigDecimal refHigh) {
        if (values == null || values.isEmpty()) return "";
        if (values.size() == 1) {
            return String.format(
                    "<svg width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\">" +
                            "<circle cx=\"%d\" cy=\"%d\" r=\"5\" fill=\"#2563eb\" stroke=\"#ffffff\" stroke-width=\"2\"/></svg>",
                    w, h, w / 2, h / 2);
        }

        int padX = 4, padY = 8;
        double useW = w - padX * 2.0;
        double useH = h - padY * 2.0;
        double min = values.stream().mapToDouble(BigDecimal::doubleValue).min().orElse(0);
        double max = values.stream().mapToDouble(BigDecimal::doubleValue).max().orElse(1);

        if (refLow != null) min = Math.min(min, refLow.doubleValue());
        if (refHigh != null) max = Math.max(max, refHigh.doubleValue());

        double span = Math.max(1e-9, max - min);

        StringBuilder pts = new StringBuilder();
        StringBuilder fillPts = new StringBuilder();
        fillPts.append(String.format(Locale.ENGLISH, "%.1f,%.1f ", (double) padX, (double) h));

        for (int i = 0; i < values.size(); i++) {
            double x = padX + (useW * i) / (values.size() - 1);
            double y = padY + (1.0 - (values.get(i).doubleValue() - min) / span) * useH;
            if (i > 0) pts.append(" ");
            pts.append(String.format(Locale.ENGLISH, "%.1f,%.1f", x, y));
            fillPts.append(String.format(Locale.ENGLISH, "%.1f,%.1f ", x, y));
            if (i == values.size() - 1) {
                fillPts.append(String.format(Locale.ENGLISH, "%.1f,%.1f", x, (double) h));
            }
        }

        StringBuilder refBands = new StringBuilder();
        if (refLow != null) {
            double rfY = padY + (1.0 - (refLow.doubleValue() - min) / span) * useH;
            refBands.append(String.format(Locale.ENGLISH,
                    "<line x1=\"%d\" y1=\"%.1f\" x2=\"%d\" y2=\"%.1f\" stroke=\"#94a3b8\" stroke-width=\"1\" stroke-dasharray=\"4 4\"/>",
                    padX, rfY, w - padX, rfY));
        }
        if (refHigh != null) {
            double rfY = padY + (1.0 - (refHigh.doubleValue() - min) / span) * useH;
            refBands.append(String.format(Locale.ENGLISH,
                    "<line x1=\"%d\" y1=\"%.1f\" x2=\"%d\" y2=\"%.1f\" stroke=\"#94a3b8\" stroke-width=\"1\" stroke-dasharray=\"4 4\"/>",
                    padX, rfY, w - padX, rfY));
        }

        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            double x = padX + (useW * i) / (values.size() - 1);
            double y = padY + (1.0 - (values.get(i).doubleValue() - min) / span) * useH;
            boolean isLast = i == values.size() - 1;
            dots.append(String.format(Locale.ENGLISH,
                    "<circle cx=\"%.1f\" cy=\"%.1f\" r=\"%d\" fill=\"%s\" stroke=\"%s\" stroke-width=\"2\"/>",
                    x, y, isLast ? 5 : 3, isLast ? "#2563eb" : "#ffffff", isLast ? "#ffffff" : "#2563eb"));
        }

        return String.format(
                "<svg width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\">" +
                        "<polygon points=\"%s\" fill=\"#93c5fd\" fill-opacity=\"0.28\"/>" +
                        "%s" +
                        "<polyline points=\"%s\" fill=\"none\" stroke=\"#2563eb\" stroke-width=\"2.5\" stroke-linejoin=\"round\"/>" +
                        "%s</svg>",
                w, h, fillPts.toString(), refBands.toString(), pts.toString(), dots.toString());
    }
}
