package com.qacademy.automation.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

import com.qacademy.automation.ui.components.NavBarComponent;

/**
 * Page object for the Enrollments management screen.
 */
public class EnrollmentsPage extends BasePage {

	private final By studentSelect = By.cssSelector("#enrollment-form select[name='studentId']");
	private final By courseSelect = By.cssSelector("#enrollment-form select[name='courseId']");
	private final By enrollButton = By.cssSelector("#enrollment-form button[type='submit']");
	private final By formError = By.id("enrollment-form-error");

	public EnrollmentsPage(WebDriver driver) {
		super(driver);
	}

	public void enrollStudent(String studentId, String courseId) {
		new Select(waitForVisible(studentSelect)).selectByValue(studentId);
		new Select(waitForVisible(courseSelect)).selectByValue(courseId);
		click(enrollButton);
	}

	public String getFormError() {
		return getText(formError);
	}

	public void setGrade(String enrollmentId, String grade) {
		type(By.id("grade-input-" + enrollmentId), grade);
		click(By.cssSelector("[data-set-grade='" + enrollmentId + "']"));
	}

	public String getGrade(String enrollmentId) {
		return getText(By.cssSelector("tr[data-id='" + enrollmentId + "'] td:nth-child(4)"));
	}

	public boolean isEnrollmentRowPresent(String enrollmentId) {
		return isDisplayed(By.cssSelector("tr[data-id='" + enrollmentId + "']"));
	}

	/** Deletes an enrollment and accepts the browser's native confirm dialog. */
	public void deleteEnrollment(String enrollmentId) {
		click(By.cssSelector("[data-delete-enrollment='" + enrollmentId + "']"));
		acceptAlert();
	}

	public int getRowCount() {
		return driver.findElements(By.cssSelector("#enrollments-table tbody tr")).size();
	}

	public NavBarComponent navBar() {
		return new NavBarComponent(driver);
	}
}
