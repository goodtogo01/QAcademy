package com.qacademy.automation.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.qacademy.automation.ui.components.NavBarComponent;

/**
 * Page object for the home/dashboard screen.
 */
public class HomePage extends BasePage {

	 private final By statGrid = By.id("stat-grid");

	 public HomePage (WebDriver driver) {
		 super(driver);
	 }
	 
	 public boolean isLoaded() {
		 return isDisplayed(statGrid);
	 }

	 public NavBarComponent navBar() {
		 return new NavBarComponent(driver);
	 }
}
