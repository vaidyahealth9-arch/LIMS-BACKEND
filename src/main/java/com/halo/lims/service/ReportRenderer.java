package com.halo.lims.service;

import com.halo.lims.dto.report.DiagnosticReportDTO;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

@Service
public class ReportRenderer {

    private final TemplateEngine templateEngine;

    public ReportRenderer(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String renderReportHtml(DiagnosticReportDTO reportDto) {
        try {
            Context context = new Context(Locale.ENGLISH);
            context.setVariable("report", reportDto);
            return templateEngine.process("report-template", context);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to render report HTML", ex);
        }
    }

    public byte[] renderReportPdf(DiagnosticReportDTO reportDto) {
        try {
            String html = renderReportHtml(reportDto);
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(html, null);
                builder.toStream(outputStream);
                builder.run();
                return outputStream.toByteArray();
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to render report PDF", ex);
        }
    }
}
