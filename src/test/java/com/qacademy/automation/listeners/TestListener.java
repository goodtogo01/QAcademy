package com.qacademy.automation.listeners;

import org.slf4j.Logger;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.qacademy.automation.utils.ExtentReportManager;
import com.qacademy.automation.utils.LoggerUtil;
import com.qacademy.automation.utils.ScreenshotUtils;

/**
 * TestNG ITestListener: creates one ExtentTest node per test method, logs the
 * outcome, and attaches a screenshot on failure.
 */

public class TestListener implements ITestListener {

	private static final Logger log = LoggerUtil.getLogger(TestListener.class);

	@Override
	public void onTestStart(ITestResult result) {
		String testName = result.getMethod().getMethodName();

		ExtentReportManager.getInstance().createTest(testName);
		log.info("Starting test: {}", testName);
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		String testName = result.getMethod().getMethodName();

		ExtentReportManager.getTest().log(Status.PASS, "Test Passed");
		log.info("Test Passed: {}", testName);
	}

	@Override
	public void onTestFailure(ITestResult result) {
		String testName = result.getMethod().getMethodName();
		ExtentTest test = ExtentReportManager.getTest();
		test.log(Status.FAIL, result.getThrowable());

		// Only a UI test has a WebDriver to screenshot; for an API test this
		// comes back null, which ScreenshotUtils already treats as expected.

		String screenshotPath = ScreenshotUtils.captureScreensShot(testName);
		if (screenshotPath != null) {
			test.addScreenCaptureFromPath(screenshotPath);
		}
		log.error("Test failed: {}", testName, result.getThrowable());
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		String testName = result.getMethod().getMethodName();
		ExtentReportManager.getTest().log(Status.SKIP, "Test Skipped");
		log.warn("Test Skipped:{}", testName);
	}
}
