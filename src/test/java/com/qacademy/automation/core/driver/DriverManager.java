package com.qacademy.automation.core.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Holds a ThreadLocal WebDriver reference so parallel test execution stays
 * thread-safe.
 *
 * TestNG can run test classes or methods on multiple threads at once
 * (parallel="methods" or "classes" in testng.xml). A plain static WebDriver
 * field would let two threads see and drive the same browser session;
 * ThreadLocal gives each thread its own private WebDriver instance while every
 * page object and test still just calls DriverManager.getDriver() without
 * knowing or caring which thread it's on.
 */

public final class DriverManager {
	private static final ThreadLocal<WebDriver> DRIVER_THREAD_LOCAL = new ThreadLocal<>();

	private DriverManager() {
		// static holder - never instantiated
	}

	public static WebDriver getDriver() {
		WebDriver driver = DRIVER_THREAD_LOCAL.get();
		if (driver == null) {
			throw new IllegalStateException(
					"No WebDriver set for this thread. Call DriverManager.setDriver(...) before getDriver().");
		}
		return driver;
	}

	/**
	 * True if the current thread has a WebDriver set. Lets callers that run for both UI
	 * and API tests (e.g. the failure-screenshot hook in TestListener) check first,
	 * instead of relying on catching the IllegalStateException getDriver() throws for the
	 * expected "this is an API test, there's no browser" case.
	 */
	public static boolean hasDriver() {
		return DRIVER_THREAD_LOCAL.get() != null;
	}

	public static void setDriver(WebDriver driver) {
		DRIVER_THREAD_LOCAL.set(driver);
	}

	public static void closeDriver() {
		WebDriver driver = DRIVER_THREAD_LOCAL.get();
		if (driver != null) {
			driver.quit();
			DRIVER_THREAD_LOCAL.remove();
		}
	}
}
