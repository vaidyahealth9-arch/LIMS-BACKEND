package com.halo.lims.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestAuditLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestAuditLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long startTimeMs = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startTimeMs;
            String path = request.getRequestURI();
            String maskedPath = maskPiiInPath(path);
            logger.info("AUDIT request method={} path={} status={} durationMs={}",
                    request.getMethod(),
                    maskedPath,
                    response.getStatus(),
                    durationMs);
        }
    }

    /**
     * Simple PII masking for production logs.
     * Replaces segments that look like IDs (P-XXXX, INV-XXXX, etc.) with [ID].
     */
    private String maskPiiInPath(String path) {
        if (path == null) return "";
        return path.replaceAll("/(P-|INV-|ENC-|an-|MT-)[a-zA-Z0-9_-]+", "/[ID]")
                   .replaceAll("/\\d+", "/[ID]");
    }
}
