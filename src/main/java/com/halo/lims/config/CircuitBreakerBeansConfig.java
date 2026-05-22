package com.halo.lims.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Circuit Breaker Configuration for LIMS Backend.
 * Prevents cascading failures when external services are down.
 *
 * <p>Note: {@link com.halo.lims.service.PhrInternalClient} currently uses its own
 * lightweight inline circuit-breaker (AtomicInteger + Instant) for PHR HTTP calls.
 * The {@code phrServiceCircuitBreaker} bean below is ready to replace that inline
 * logic when a full Resilience4j migration is done. Until then, these beans are
 * available for any new service clients that need declarative circuit breaking.
 */
@Configuration
public class CircuitBreakerBeansConfig {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerBeansConfig.class);

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50.0f)  // 50% failure rate triggers open
                .slowCallRateThreshold(100.0f)  // 100% of calls are slow
                .slowCallDurationThreshold(Duration.ofSeconds(2))  // Calls >2 sec are slow
                .waitDurationInOpenState(Duration.ofSeconds(30))  // Wait 30 sec before retry
                .permittedNumberOfCallsInHalfOpenState(3)  // Allow 3 calls in half-open
                .minimumNumberOfCalls(10)  // Need 10 calls to calculate rate
                .automaticTransitionFromOpenToHalfOpenEnabled(true)  // Auto-retry after wait
                .recordExceptions(Exception.class)  // Record all exceptions
                .ignoreExceptions(IllegalArgumentException.class)  // Don't count validation errors
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(defaultConfig);

        // Log circuit breaker lifecycle events
        registry.getEventPublisher()
                .onEntryAdded(event -> log.info("Circuit breaker created: {}", 
                        event.getAddedEntry().getName()))
                .onEntryRemoved(event -> log.info("Circuit breaker removed: {}", 
                        event.getRemovedEntry().getName()));

        return registry;
    }

    /**
     * Circuit breaker for PHR service calls.
     * Shorter wait duration (60 sec) and stricter thresholds.
     */
    @Bean
    public CircuitBreaker phrServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50.0f)
                .slowCallRateThreshold(100.0f)
                .slowCallDurationThreshold(Duration.ofSeconds(3))  // PHR is slower
                .waitDurationInOpenState(Duration.ofSeconds(60))  // Longer wait
                .permittedNumberOfCallsInHalfOpenState(2)  // Fewer test calls
                .minimumNumberOfCalls(5)  // Faster detection
                .build();

        CircuitBreaker breaker = registry.circuitBreaker("phr-service", config);

        breaker.getEventPublisher()
                .onStateTransition(event -> {
                    var transition = event.getStateTransition();
                    log.warn("PHR service circuit breaker: {} -> {}", 
                            transition.getFromState(),
                            transition.getToState());
                });

        return breaker;
    }

    /**
     * Circuit breaker for database operations.
     */
    @Bean
    public CircuitBreaker databaseCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(75.0f)  // Higher threshold for database
                .slowCallRateThreshold(100.0f)
                .slowCallDurationThreshold(Duration.ofSeconds(5))  // DB can be slow
                .waitDurationInOpenState(Duration.ofSeconds(120))  // Longer wait before retry
                .permittedNumberOfCallsInHalfOpenState(1)  // Very conservative
                .minimumNumberOfCalls(3)  // Quick detection
                .build();

        return registry.circuitBreaker("database", config);
    }
}

