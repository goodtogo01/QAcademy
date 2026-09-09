package com.qacademy.automation.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page object for the Students management screen.
 */
public class StudentsPage extends BasePage {
	
	private final By firstNameInput = By.cssSelector("#student-form input[name='firstName']");
	private final By lastNameInput = By.cssSelector("#student-form input[name='lastName']");
	private final By emailInput = By.cssSelector("#student-form input[name='email']");
	private final By dobInput = By.cssSelector("#student-form input[name='dateOfBirth']");
	private final By saveButton = By.cssSelector("#student-form input[name='submit']");
	private final By formError = By.id("student-form-error");
	
	
	public StudentsPage(WebDriver driver) {
		super(driver);
	}
	
	 /** dateOfBirth must be DD/MM/YYYY - that's what the form's pattern attribute enforces. */
	public void addStudent(String firstName, String lastName, String email, String dateOfBirth) {
		type(firstNameInput, firstName);
		type(lastNameInput, lastName);
		type(emailInput, email);
		type(dobInput, dateOfBirth);
		click(saveButton);
	}
	
	public String getFormError() {
		return getText(formError);
	}
	public boolean isStudentRowPresent(String studentId) {
		return isDisplayed(By.cssSelector("tr[data-id='" + studentId + "']"));
	}
	 /** Deletes a student and accepts the browser's native "This cannot be undone" confirm dialog. */
	public void deleteStudent(String studentId) {
		click(By.cssSelector("[data-delete-student='" + studentId + "']"));
		acceptAlert();
	}
	
	public int getRowCount() {
		return driver.findElements(By.cssSelector("#students-table tbody tr")).size();
	}
}
