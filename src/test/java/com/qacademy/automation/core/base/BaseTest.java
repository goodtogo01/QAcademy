package com.qacademy.automation.core.base;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.qacademy.automation.core.backend.BackendLifecycleManager;
import com.qacademy.automation.utils.ExtentReportManager;

/**
 * Shared BeforeSuite/AfterSuite hooks, e.g. ExtentReports initialization.
 *
 * Every UI test (via BaseUiTest) and every API test (via BaseApiTest) extends this class,
 * so whatever runs here runs once per TestNG suite - no matter how many test classes or
 * methods TestNG spreads across threads, because TestNG only invokes an inherited
 * @BeforeSuite/@AfterSuite once per suite, not once per subclass.
 *
 * IMPORTANT - "once per suite" is not "once per regression run": testng-regression.xml
 * combines testng-ui.xml, testng-api.xml and testng-e2e.xml via {@code <suite-files>},
 * and each of those is its own top-level &lt;suite&gt; - so a full regression run fires
 * beforeSuite()/afterSuite() three times in the same JVM, not once. That is exactly why
 * afterSuite() below does NOT stop the backend: doing so used to kill it after the UI
 * suite finished and force a full cold restart before the API suite's own beforeSuite()
 * could run - adding a minute of dead time per re-run and, worse, occasionally losing the
 * race with the OS releasing the just-killed process's H2 file lock (the exact "Database
 * may be already in use" failure BackendLifecycleManager's own error message warns about),
 * which could take the entire API suite down with a beforeSuite() failure that had nothing
 * to do with any individual API test. BackendLifecycleManager.ensureRunning() is cheap and
 * idempotent when the backend is already up (one fast HTTP call), so letting it run again
 * for the API and E2E suites costs nothing and is what makes this safe.
 */

public abstract class BaseTest {

	@BeforeSuite(alwaysRun = true)
	public void beforeSuite() {
		// Solves "I clicked Run without starting Spring Boot first": checks the real
		// backend over HTTP, auto-starts it if it isn't up, and blocks here until it's
		// actually ready - or fails the suite immediately with one clear message instead
		// of forty confusing per-test failures downstream. Runs before ExtentReports init
		// so a backend problem is reported as exactly that, not as a wall of test failures.
		//
		// Fires once per included suite-file (see class Javadoc), so on the API and E2E
		// suites this is normally just one fast "yes, still up" HTTP call - it only
		// actually starts anything the first time, in whichever suite runs first.
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

		// Deliberately does NOT call BackendLifecycleManager.shutdownIfStartedByUs() here -
		// see the class Javadoc for why. The backend this framework started is cleaned up
		// by the JVM shutdown hook BackendLifecycleManager registers when it launches the
		// process, which fires once, when the whole `mvn test` JVM actually exits - after
		// the UI, API and E2E suites have all finished, not after each one.
	}

}
