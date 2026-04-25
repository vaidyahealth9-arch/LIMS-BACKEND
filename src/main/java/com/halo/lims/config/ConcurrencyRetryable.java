package com.halo.lims.config;

import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Reusable retry policy for write operations that can fail due to transient
 * concurrency conflicts.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Retryable(
        retryFor = {
                OptimisticLockingFailureException.class,
                ObjectOptimisticLockingFailureException.class,
                OptimisticLockException.class,
                CannotAcquireLockException.class,
                PessimisticLockingFailureException.class,
                DeadlockLoserDataAccessException.class
        },
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2.0)
)
public @interface ConcurrencyRetryable {
}