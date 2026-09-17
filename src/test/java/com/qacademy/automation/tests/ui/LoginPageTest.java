package com.qacademy.automation.tests.ui;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.qacademy.automation.config.ConfigManager;
import com.qacademy.automation.core.base.BaseUiTest;
import com.qacademy.automation.data.UserRole;
import com.qacademy.automation.ui.pages.HomePage;
import com.qacademy.automation.ui.pages.LoginPage;

/**
 * UI test class for the login flow.
 */
public class LoginPageTest extends BaseUiTest {

	private LoginPage loginPage;

	@BeforeMethod(alwaysRun = true)
	public void openLoginPage() {
		getDriver().get(ConfigManager.getInstance().getBaseUrl());
		// Constructed here, not as a field initializer - a field initializer runs during
		// object construction, before this @BeforeMethod (and therefore before
		// BaseUiTest's own @BeforeMethod actually creates the WebDriver), so it would
		// call getDriver() while DriverManager has no driver set yet and crash immediately.
		loginPage = new LoginPage(getDriver());
	}

	@Test
	public void login_withValidAdminCredentials_landsOnHomePage() {
		HomePage homePage = loginPage.login(UserRole.ADMIN.getUserName(), UserRole.ADMIN.getPassword());

		Assert.assertTrue(homePage.isLoaded(), "Home Page dashboard should be visible after a valid login");
		Assert.assertEquals(homePage.navBar().getLoggedInUsername(), UserRole.ADMIN.getUserName());
		Assert.assertEquals(homePage.navBar().getRole(), "ADMIN");
	}

	@Test
	public void login_withWrongPassword_showsInvalidCredentialsError() {
		loginPage.enterUsername(UserRole.ADMIN.getUserName());
		loginPage.enterPassword("WrongPassword1!");
		loginPage.clickSignin();

		Assert.assertTrue(loginPage.isErrorDisplayed(), "An Error message should displayed for a wrong password ");
		Assert.assertEquals(loginPage.getErrorMessage(), "Invalid username or password.");
	}

	@Test
	public void clickDemoUserChip_prefillsCredentials_andLogsIn() {
		loginPage.clickDemoUser(UserRole.STAFF.getUserName());
		loginPage.clickSignin();

		HomePage homePage = new HomePage(getDriver());
		Assert.assertTrue(homePage.isLoaded(), "Home page should load after signing in via a demo-account chip.");
		Assert.assertEquals(homePage.navBar().getRole(), "STAFF");
	}
}
