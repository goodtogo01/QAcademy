package com.qacademy.automation.ui.pages;

import java.time.Duration;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.qacademy.automation.config.ConfigManager;

/**
 * Common explicit waits and the driver reference every page object extends.
 */
public abstract class BasePage {
	protected final WebDriver driver;
	protected final WebDriverWait wait;
	
	
	
	protected BasePage(WebDriver driver) {
		this.driver=driver;
		int explicitWaitSeconds = ConfigManager.getInstance().getExplicitWaitSeconds();
		this.wait=new WebDriverWait(driver, Duration.ofSeconds(explicitWaitSeconds));
	}
    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
	
	protected WebElement waitForClickable (By locator) {
		return wait.until(ExpectedConditions.elementToBeClickable(locator));
	}

	protected void click(By locator) {
		waitForClickable(locator).click();
	}
	
	protected void type(By locator, String text) {
		WebElement element = waitForVisible(locator);
		element.clear();
		element.sendKeys(text);
	}
	
	protected String getText(By locator) {
		return waitForVisible(locator).getText();
	}
	
	protected boolean isDisplayed(By locator) {
		try {
			return driver.findElement(locator).isDisplayed();
		}catch(NoSuchElementException e) {
			return false;
		}
	}

	/**
	 * Like isDisplayed(), but waits up to the explicit-wait timeout for the element to
	 * become visible instead of checking the DOM exactly once.
	 *
	 * isDisplayed() is correct as a fast, one-shot check when it's already being polled by
	 * an outer WebDriverWait (e.g. waiting for a row to disappear after a delete). But used
	 * bare - right after clicking submit on a form whose result comes back from an async
	 * fetch() call - a one-shot check races the network call and fails intermittently
	 * depending on how fast that response happens to arrive. Anywhere a test asks "is this
	 * confirmation/error/dashboard visible after that action", it should wait for it.
	 */
	protected boolean isVisibleWithinWait(By locator) {
		try {
			return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)) != null;
		} catch (TimeoutException e) {
			return false;
		}
	}
	/** Waits for and accepts a native JS confirm()/alert() dialog - the app uses these on delete actions. */
	protected void acceptAlert() {
		wait.until(ExpectedConditions.alertIsPresent());
		driver.switchTo().alert().accept();
	}
}