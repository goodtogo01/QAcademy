package com.qacademy.automation.core.backend;

/**
 * Thrown by {@link BackendLifecycleManager} when the qAcademy backend never becomes
 * reachable - either it failed to start, or it started but never answered a health
 * check within the configured timeout.
 *
 * Deliberately unchecked, and deliberately thrown out of @BeforeSuite so it fails the
 * whole suite immediately with one clear message, instead of letting every individual
 * UI/API test fail on its own with a confusing "connection refused" or
 * NoSuchElementException.
 */
public class BackendNotAvailableException extends RuntimeException {

	public BackendNotAvailableException(String message) {
		super(message);
	}

	public BackendNotAvailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
