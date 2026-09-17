package com.qacademy.automation.utils;

import java.time.Duration;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.qacademy.automation.config.ConfigManager;

/**

 * Explicit-wait helper methods for Selenium, built around the driver rather than a page object 
 * so a page's BasePage can delegate to these instead of duplicating them, 
 * and a test class can use them directly when there's no page object involved (e.g. waiting on a raw URL change).
 */

public final class WaitUtils {
	
	private WaitUtils() {
		   // static holder - never instantiated
	}
	
	private static WebDriverWait wait(WebDriver driver) {
		int explicitWaitSeconds = ConfigManager.getInstance().getExplicitWaitSeconds();
		return new WebDriverWait(driver, Duration.ofSeconds(explicitWaitSeconds));
	}
	
	public static WebElement waitForVisible(WebDriver driver, By locator) {
		return wait(driver).until(ExpectedConditions.visibilityOfElementLocated(locator));
	}
	
	public static WebElement waitForClickable(WebDriver driver, By locator) {
		return wait(driver).until(ExpectedConditions.elementToBeClickable(locator));
	}
	public static boolean waitForInvisible(WebDriver driver, By locator) {
		return wait(driver).until(ExpectedConditions.invisibilityOfElementLocated(locator));
	}
	public static boolean waitForTextPresent(WebDriver driver, By locator, String text) {
		return wait(driver).until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
	}
	public static boolean waitForUrlContains(WebDriver driver, String fragment) {
		return wait(driver).until(ExpectedConditions.urlContains(fragment));
	}
	public static Alert waitForAlert(WebDriver driver) {
		wait(driver).until(ExpectedConditions.alertIsPresent());
		return driver.switchTo().alert();
	}
}
