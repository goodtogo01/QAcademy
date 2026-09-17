package com.qacademy.automation.core.base;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.qacademy.automation.core.backend.BackendLifecycleManager;
import com.qacademy.automation.utils.ExtentReportManager;

/**
 * Shared BeforeSuite/AfterSuite hooks, e.g. ExtentReports initialization.
 *
 * Every UI test (via BaseUiTest) and every API test (via BaseApiTest) extends this class,
 * so whatever runs here runs exactly once per TestNG suite - no matter how many test
 * classes or methods TestNG spreads across threads - because TestNG only invokes an
 * inherited @BeforeSuite/@AfterSuite once per suite, not once per subclass.
 */

public abstract class BaseTest {

	@BeforeSuite(alwaysRun = true)
	public void beforeSuite() {
		// Solves "I clicked Run without starting Spring Boot first": checks the real
		// backend over HTTP, auto-starts it if it isn't up, and blocks here until it's
		// actually ready - or fails the suite immediately with one clear message instead
		// of forty confusing per-test failures downstream. Runs before ExtentReports init
		// so a backend problem is reported as exactly that, not as a wall of test failures.
		BackendLifecycleManager.getInstance().ensureRunning();

		// Touching getInstance() here is optional - the Bill Pugh holder would lazy-init on
		// the first TestListener.onTestStart() anyway - but doing it explicitly means a
		// broken extent-config.xml fails loudly at suite start, not on the first test.
		ExtentReportManager.getInstance();
		System.out.println("Test suite starting...");
	}

	@AfterSuite(alwaysRun = true)
	public void afterSuite() {
		ExtentReportManager.getInstance().flush();
		System.out.println("Test suite finished.");

		// No-op unless beforeSuite() above was the one that actually started the backend -
		// see BackendLifecycleManager's ownership rule. Runs last so the report is flushed
		// even if stopping the backend process itself hits a snag.
		BackendLifecycleManager.getInstance().shutdownIfStartedByUs();
	}

}
