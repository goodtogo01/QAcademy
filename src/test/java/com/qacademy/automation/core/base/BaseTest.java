package com.qacademy.automation.core.base;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

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
        // TODO: once utils/ExtentReportManager.java is implemented, initialize the shared
        // ExtentReports instance here, e.g. ExtentReportManager.getInstance();
        System.out.println("Test suite starting...");
	}
	
	@AfterSuite(alwaysRun = true)
	public void afterSuite() {
		 // TODO: once utils/ExtentReportManager.java is implemented, flush the report here,
        // e.g. ExtentReportManager.getInstance().flush();
        System.out.println("Test suite finished.");
	}

}
