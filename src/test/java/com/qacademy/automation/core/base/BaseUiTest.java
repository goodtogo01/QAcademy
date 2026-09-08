package com.qacademy.automation.core.base;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.qacademy.automation.core.driver.DriverFactory;
import com.qacademy.automation.core.driver.DriverManager;

/**
 * Extends BaseTest; manages WebDriver lifecycle for every UI test.
 */
public abstract class BaseUiTest extends BaseTest {

	@BeforeMethod(alwaysRun = true)
	public void setUpDriver() {
		WebDriver driver = DriverFactory.createDriver();
		DriverManager.setDriver(driver);
	}

	@AfterMethod(alwaysRun = true)
	public void tearDown() {
		DriverManager.closeDriver();
	}

	protected WebDriver getDriver() {
		return DriverManager.getDriver();
	}
}
