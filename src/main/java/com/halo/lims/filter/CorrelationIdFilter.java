package com.halo.lims.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Request correlation ID filter for distributed tracing.
 * 
 * Adds X-Correlation-ID to request/response and stores in MDC for logging.
 * Enables tracking of requests across multiple services.
 */
@Slf4j
public class CorrelationIdFilter extends OncePerRequestFilter {
    
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String MDC_CORRELATION_ID = "traceId";
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        // Extract or generate correlation ID
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        
        // Put in MDC for structured logging
        MDC.put(MDC_CORRELATION_ID, correlationId);
        
        // Add to response header
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        
        log.debug("{} {} - traceId: {}", request.getMethod(), request.getRequestURI(), correlationId);
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clean up MDC
            MDC.remove(MDC_CORRELATION_ID);
        }
    }
}
