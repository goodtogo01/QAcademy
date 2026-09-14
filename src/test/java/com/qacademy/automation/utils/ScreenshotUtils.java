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
     * @return the absolute path of the saved screenshot, or null if capture failed.
     * Capture failures are logged, not thrown.
     * a screenshot problem shouldn't mask the real test failure that triggered it.
     */
	
	public static String captureScreensShot(String testName) {
		try {
			WebDriver driver = DriverManager.getDriver();
			File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			
			Path targetDir = Paths.get(SCREENSHOT_DIR);
			Files.createDirectories(targetDir);
			
			String fileName = testName+"_"+LocalDateTime.now().format(TIMESTAMP)+".png";
			Path target = targetDir.resolve(fileName);
			Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
			
			return target.toAbsolutePath().toString();
		}catch (IllegalStateException | IOException e) {
			log.warn("Failed to capture screenshot for test '{}'", testName, e);
            return null;
		}
	}
}
