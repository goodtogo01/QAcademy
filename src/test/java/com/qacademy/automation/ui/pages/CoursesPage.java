package com.qacademy.automation.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.qacademy.automation.ui.components.NavBarComponent;

/**
 * Page object for the Courses management screen.
 */
public class CoursesPage extends BasePage {

    private final By nameInput = By.cssSelector("#course-form input[name='name']");
    private final By creditsInput = By.cssSelector("#course-form input[name='credits']");
    private final By saveButton = By.cssSelector("#course-form button[type='submit']");
    private final By formError = By.id("course-form-error");

    public CoursesPage(WebDriver driver) {
        super(driver);
    }
    public void addCourse(String name, String credits) {
    	type(nameInput, name);
    	type(creditsInput, credits);
    	click(saveButton);
    }
    public String getFormError() {
    	return getText(formError);
    }
    
    public int getRowCount() {
    	return driver.findElements(By.cssSelector("#courses-table tbody tr")).size();
    }

    public NavBarComponent navBar() {
        return new NavBarComponent(driver);
    }
    
}
