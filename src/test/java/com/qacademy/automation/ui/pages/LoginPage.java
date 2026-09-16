package com.qacademy.automation.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page object for the login screen.
 */
public class LoginPage extends BasePage{
	private final By usernameInput = By.id("login-username");
	private final By passwordInput = By.id("login-password");
	private final By signInButton = By.cssSelector("#login-form button[type='submit']");
	private final By errorMessage = By.id("login-error");
	private final By showRegisterLink = By.id("show-register");
	
	public LoginPage(WebDriver driver) {
		super(driver);
	}

	public void enterUsername(String username) {
		type(usernameInput, username);
	}
	public void enterPassword(String password) {
		type(passwordInput, password);
	}
	
	public void clickSignin() {
		click(signInButton);
	}
	
	

    /** Happy-path login - use this when the test expects to land on HomePage. */
	public HomePage login(String username, String password) {
		enterUsername(username);
		enterPassword(password);
		clickSignin();
		return new HomePage(driver);
	}
	
	/** Clicks a demo-account chip (admin/staff/student), which pre-fills both fields. */
	public void clickDemoUser(String role) {
		click(By.cssSelector("[data-demo-user='" + role + "']"));
	}
	
	public String getErrorMessage() {
		return getText(errorMessage);
	}
	
	public boolean isErrorDisplayed() {
		return isDisplayed(errorMessage);
	}
	
	public RegisterPage goToRegister() {
	    click(showRegisterLink);
	    return new RegisterPage(driver);
	}
}
