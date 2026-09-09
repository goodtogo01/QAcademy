package com.qacademy.automation.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page object for the home/dashboard screen.
 */
public class HomePage extends BasePage {
	
	 private final By statGrid = By.id("stat-grid");
	 private final By usernameDisplay = By.id("username-display");
	 private final By roleBadge = By.id("role-badge");
	 private final By logoutButton = By.id("logout-btn");
	 
	 public HomePage (WebDriver driver) {
		 super(driver);
	 }
	 
	 public boolean isLoaded() {
		 return isDisplayed(statGrid);
	 }
	 public String getLoggedInUseName() {
		 return getText(usernameDisplay);
	 }
	 
	 public String getRole() {
		 return getText(roleBadge);
	 }
	 
	 public StudentsPage goToStudents() {
		 click(By.cssSelector(".topnav a[data-route='students']"));
		 return new StudentsPage(driver);
	 }

	 public CoursesPage goToCourses() {
		 click(By.cssSelector(".topnav a[data-route='courses']"));
		 return new CoursesPage(driver);
	 }
	 
	 public EnrollmentsPage goToEnrollments() {
		 click(By.cssSelector(".topnav a[data-route='enrollments']"));
		 return new EnrollmentsPage(driver);
	 }
	 
	 public LoginPage logout() {
		 click(logoutButton);
		 return new LoginPage(driver);
	 }
}
