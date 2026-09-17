package com.qacademy.automation.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;

import com.qacademy.automation.core.driver.DriverManager;

/**
 * Captures a screenshot on UI test failure. Called from a TestNG listener,
 * not a page object, so it reads the current thread's driver from
 * DriverManager rather than taking one as a parameter.
 */


public final class ScreenshotUtils {
	private static final Logger log = LoggerUtil.getLogger(ScreenshotUtils.class);
	private static final String SCREENSHOT_DIR = "test-output/screenshot";
	private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");

	private ScreenshotUtils() {
		// static holder - never instantiated
	}
	
	 /**
     * @return the absolute path of the saved screenshot, or null if there was nothing to
     * capture (an API test - no browser ever existed for this thread) or capture itself
     * failed. Either way this only logs, it never throws - a screenshot problem, or the
     * complete absence of a browser, must never mask the real test failure that triggered
     * this call in the first place.
     */
	
	public static String captureScreenshot(String testName) throws IllegalStateException {
		if (!DriverManager.hasDriver()) {
			// Expected for every API test failure - there's no browser on this thread to
			// screenshot. Not a warning: this is the normal case for half the suite, not a
			// capture problem, so it stays out of the log at anything above debug.
			log.debug("No WebDriver for this thread - skipping screenshot for test '{}' (API test).", testName);
			return null;
		}
		try {
			WebDriver driver = DriverManager.getDriver();
			File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			
			Path targetDir = Paths.get(SCREENSHOT_DIR);
			Files.createDirectories(targetDir);
			
			String fileName = testName+"_"+LocalDateTime.now().format(TIMESTAMP)+".png";
			Path target = targetDir.resolve(fileName);
			Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
			
			return target.toAbsolutePath().toString();
		}catch (IOException | RuntimeException e) {
			// RuntimeException also covers Selenium's own WebDriverException family (e.g. a
			// session that died between the test failing and this hook running) - a broken
			// screenshot attempt must never escape and take the real failure report down with it.
			log.warn("Failed to capture screenshot for test '{}'", testName, e);
            return null;
		}
	}
}
