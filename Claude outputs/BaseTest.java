package com.qacademy.automation.core.base;

import com.qacademy.automation.core.backend.BackendLifecycleManager;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * Shared suite-level lifecycle for every test in the framework - both lanes
 * ({@code BaseUiTest} and {@code BaseApiTest}, per Automation Framework Design §2)
 * extend this, so every test class in the suite inherits it without doing anything
 * itself.
 *
 * <p>{@code @BeforeSuite}/{@code @AfterSuite} (not {@code @BeforeClass} or
 * {@code @BeforeMethod}) is deliberate: TestNG runs a {@code @BeforeSuite} method
 * exactly once, before the first test in the entire suite - whether that suite has one
 * test class or forty. Putting the backend check here means "is the backend up" gets
 * answered once per run, not once per test class (wasteful) or once per test method
 * (very wasteful, and would fight itself under parallel execution).</p>
 *
 * <p>{@code alwaysRun = true} on both methods matters here specifically: without it, if
 * a {@code @BeforeSuite} in some other class threw before this one ran, TestNG could
 * skip this cleanup pairing entirely, in the worst case leaving a backend process this
 * framework started still running with nothing left alive to stop it.</p>
 */
public abstract class BaseTest {

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        // Solves the exact "I clicked Run without starting Spring Boot" concern:
        // checks the real backend over HTTP, auto-starts it if needed, and blocks
        // here until it's actually ready - or fails the suite immediately, with one
        // clear message, instead of forty confusing per-test failures downstream.
        BackendLifecycleManager.getInstance().ensureRunning();

        // ExtentReportManager.getInstance().init(); goes here once Design §4 step 7 is built.
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        // No-op unless beforeSuite() above was the one that actually started the
        // backend - see BackendLifecycleManager's ownership rule.
        BackendLifecycleManager.getInstance().shutdownIfStartedByUs();

        // ExtentReportManager.getInstance().flush(); goes here once Design §4 step 7 is built.
    }
}
