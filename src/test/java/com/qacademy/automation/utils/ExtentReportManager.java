package com.qacademy.automation.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.qacademy.automation.config.ConfigManager;

/**
 * Singleton holding the one shared ExtentReports instance. Uses the same
 * Bill Pugh holder idiom as ConfigManager: the report file is only created
 * the first time getInstance() actually runs.
 *
 * A ThreadLocal<ExtentTest> mirrors DriverManager - when TestNG runs tests
 * in parallel, each thread needs to log to its own report node.
 */


public final class ExtentReportManager {

	private static final String REPORT_PATH = "test-output/ExtentReport.html";
	private static final ThreadLocal<ExtentTest> TEST_THREAD_LOCAL = new ThreadLocal<>();
	private final ExtentReports extentReports;
	
	private ExtentReportManager() {
		ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_PATH);
		spark.config().setDocumentTitle("qAcademy Automation Report");
		spark.config().setReportName("qAcademy Test Execution Report");
		spark.config().setTheme(Theme.STANDARD);
		
		extentReports = new ExtentReports();
		extentReports.attachReporter(spark);
		extentReports.setSystemInfo("Environment", ConfigManager.getInstance().getEnvironment().name());
		extentReports.setSystemInfo("Browser", ConfigManager.getInstance().getBrowser());
		
	}
	private static final class Holder{
		private static final ExtentReportManager INSTANCE = new ExtentReportManager();
	}
	public static ExtentReportManager getInstance() {
		return Holder.INSTANCE;
	}
	
	public ExtentTest createTest(String testName) {
		ExtentTest test = extentReports.createTest(testName);
		TEST_THREAD_LOCAL.set(test);
		return test;
	}
	
	public static ExtentTest getTest() {
		return TEST_THREAD_LOCAL.get();
	}
	
	public void flush() {
		extentReports.flush();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
