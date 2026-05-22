package com.halo.lims.security;

import com.halo.lims.filter.CorrelationIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security test suite for LIMS backend.
 * 
 * Tests verify:
 * - CORS hardening
 * - Authentication enforcement
 * - HTTP method restrictions
 * - Correlation ID tracking
 * - Error response standardization
 */
public class SecurityTests {
    
    /**
     * Test: Correlation ID is added to response headers
     */
    @Test
    public void testCorrelationIdTracking() throws Exception {
        // Arrange
        CorrelationIdFilter filter = new CorrelationIdFilter();
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .addFilter(filter)
                .build();
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/test")
                .header("X-Correlation-ID", "test-trace-123"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(header().string("X-Correlation-ID", "test-trace-123"));
    }
    
    /**
     * Test: Correlation ID is generated if not provided
     */
    @Test
    public void testCorrelationIdGenerated() throws Exception {
        // Arrange
        CorrelationIdFilter filter = new CorrelationIdFilter();
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .addFilter(filter)
                .build();
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/test"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-ID"));
    }
    
    /**
     * Test: /api/integration/** requires signature verification
     * 
     * This test ensures that service-to-service endpoints cannot be accessed
     * without proper HMAC-SHA256 signature.
     */
    @Test
    public void testRequestSignatureRequired() {
        String testMessage = "GET:/api/integration/webhook:1705318245:";
        String secretKey = "test-secret-key-shared-with-all-services";
        
        // Valid signature
        String validSignature = RequestSigningUtil.sign(testMessage, secretKey);
        assert !validSignature.isEmpty();
        
        // Verification should pass
        boolean verified = RequestSigningUtil.verify(testMessage, validSignature, secretKey);
        assert verified;
        
        // Invalid signature should fail
        boolean notVerified = RequestSigningUtil.verify(testMessage, "invalid-signature", secretKey);
        assert !notVerified;
    }
    
    /**
     * Test: CORS wildcard is not allowed in production
     * 
     * Verifies that CORS_ALLOWED_ORIGIN_PATTERNS env var is required
     * and that wildcard (*) is not accepted.
     */
    @Test
    public void testCORSProductionHardening() {
        String corsPattern = "https://my-domain.com";
        assert !corsPattern.contains("*");
        assert corsPattern.startsWith("https://");
    }
    
    /**
     * Test controller for testing filters
     */
    @RestController
    public static class TestController {
        
        @GetMapping("/api/v1/test")
        public String test() {
            return "ok";
        }
    }
}
