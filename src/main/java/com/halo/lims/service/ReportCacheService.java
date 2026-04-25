package com.halo.lims.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ReportCacheService {

    private final boolean cacheEnabled;
    private final Cache<Integer, byte[]> pdfCache;

    public ReportCacheService(
            @Value("${app.report.cache.enabled:false}") boolean cacheEnabled,
            @Value("${app.report.cache.max-size:200}") long maxSize,
            @Value("${app.report.cache.ttl-minutes:30}") long ttlMinutes) {
        this.cacheEnabled = cacheEnabled;
        this.pdfCache = Caffeine.newBuilder()
                .maximumSize(Math.max(1, maxSize))
                .expireAfterWrite(Duration.ofMinutes(Math.max(1, ttlMinutes)))
                .build();
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public byte[] getCachedPdf(Integer serviceRequestId) {
        if (!cacheEnabled || serviceRequestId == null) {
            return null;
        }
        return pdfCache.getIfPresent(serviceRequestId);
    }

    public void cachePdf(Integer serviceRequestId, byte[] pdf) {
        if (!cacheEnabled || serviceRequestId == null || pdf == null) {
            return;
        }
        pdfCache.put(serviceRequestId, pdf);
    }

    public void invalidateCache(Integer serviceRequestId) {
        if (serviceRequestId == null) {
            return;
        }
        pdfCache.invalidate(serviceRequestId);
    }
}
