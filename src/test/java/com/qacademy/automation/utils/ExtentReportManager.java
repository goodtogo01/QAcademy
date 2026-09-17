package com.qacademy.automation.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import com.qacademy.automation.config.ConfigManager;

/**
 * Singleton holding the one shared ExtentReports instance. Uses the same Bill Pugh holder
 * idiom as ConfigManager: the report file is only created the first time getInstance() runs.
 *
 * Report styling (theme, title, report name, ...) lives in src/test/resources/extent-config.xml
 * rather than being hardcoded here - loadXMLConfig() reads it once at construction time.
 *
 * A ThreadLocal<ExtentTest> mirrors DriverManager - when TestNG runs tests in parallel,
 * each thread needs to log to its own report node.
 */
public final class ExtentReportManager {

	private static final String REPORT_PATH = "test-output/ExtentReport.html";
	private static final String CONFIG_FILE = "extent-config.xml";

	private static final ThreadLocal<ExtentTest> TEST_THREAD_LOCAL = new ThreadLocal<>();

	private final ExtentReports extentReports;

	private ExtentReportManager() {
		ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_PATH);

		try {
			URL configUrl = getClass().getClassLoader().getResource(CONFIG_FILE);
			if (configUrl == null) {
				throw new IllegalStateException(
						"Could not find " + CONFIG_FILE + " on the test classpath (expected under src/test/resources).");
			}
			spark.loadXMLConfig(new File(configUrl.toURI()));
		} catch (IOException | URISyntaxException e) {
			throw new IllegalStateException("Failed to load " + CONFIG_FILE, e);
		}

		extentReports = new ExtentReports();
		extentReports.attachReporter(spark);
		extentReports.setSystemInfo("Author", "Khosruz zaman");
		extentReports.setSystemInfo("Environment", ConfigManager.getInstance().getEnvironment().name());
		extentReports.setSystemInfo("Browser", ConfigManager.getInstance().getBrowser());
	}

	private static final class Holder {
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
