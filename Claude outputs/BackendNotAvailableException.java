package com.qacademy.automation.core.backend;

/**
 * Thrown by {@link BackendLifecycleManager} when the qAcademy backend never becomes
 * reachable — either because it failed to start, or because it started but never
 * answered a health check within the configured timeout.
 *
 * <p>This is deliberately unchecked and deliberately fails the whole suite immediately
 * (thrown out of {@code @BeforeSuite}), rather than letting every individual UI/API test
 * fail one-by-one with its own confusing "connection refused" or
 * {@code NoSuchElementException}. One clear failure at the top beats forty confusing
 * ones at the bottom.</p>
 */
public class BackendNotAvailableException extends RuntimeException {

    public BackendNotAvailableException(String message) {
        super(message);
    }

    public BackendNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
