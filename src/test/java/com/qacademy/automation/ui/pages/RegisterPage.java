package com.qacademy.automation.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page object for the register form - the same index.html page as LoginPage,
 * just the #register-form section toggled visible instead of #login-form
 * (LoginPage.goToRegister() is what makes that switch).
 */

public class RegisterPage extends BasePage {

	private final By usernameInput = By.id("register-username");
	private final By passwordInput = By.id("register-password");
	private final By roleSelect = By.id("register-role");
	private final By registerButton = By.cssSelector("#register-form button[type='submit']");
	private final By errorMessage = By.id("register-error");
	private final By successMessage = By.id("register-success");

	public RegisterPage(WebDriver driver) {
		super(driver);
	}

	/**
	 * role must match a <option value="..."> exactly: "Admin", "Staff", or
	 * "Student".
	 */
	public void register(String username, String password, String role) {
		type(usernameInput, username);
		type(passwordInput, password);
		new Select(waitForVisible(roleSelect)).selectByValue(role);
		click(registerButton);
	}

	public boolean isSuccessDisplayed() {
		return isDisplayed(successMessage);
	}

	public String getSuccessMessage() {
		return getText(successMessage);
	}

	public boolean isErrorDisplayed() {
		return isDisplayed(errorMessage);
	}

	public String getErrorMessage() {
		return getText(errorMessage);
	}
}
