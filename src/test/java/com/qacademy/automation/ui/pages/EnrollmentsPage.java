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
		return isVisibleWithinWait(By.cssSelector("tr[data-id='" + enrollmentId + "']"));
	}

	/**
	 * Waits for a row matching this exact student name + course name to appear anywhere
	 * in the table.
	 *
	 * Deliberately identity-based rather than a row-count check: the "All enrollments"
	 * table is never reset between suite runs (the H2 file persists), so it accumulates
	 * every enrollment ever created by every past run - a plain "count went up by 1"
	 * assertion has to race that ever-growing, shared list and is vulnerable to whatever
	 * else happens to touch it. TestDataFactory gives every seeded student/course a
	 * random suffix, so the pairing of names passed in here is guaranteed unique - this
	 * only has to find its own row, regardless of how many others already exist.
	 */
	public boolean isEnrollmentPresentByNames(String studentFullName, String courseName) {
		return isVisibleWithinWait(By.xpath(
				"//table//tr[td[2][normalize-space()='" + studentFullName
				+ "'] and td[3][normalize-space()='" + courseName + "']]"));
	}

	/** Deletes an enrollment and accepts the browser's native confirm dialog. */
	public void deleteEnrollment(String enrollmentId) {
		click(By.cssSelector("[data-delete-enrollment='" + enrollmentId + "']"));
		acceptAlert();
	}

	public int getRowCount() {
		waitForTableLoaded("enrollments-table");
		return driver.findElements(By.cssSelector("#enrollments-table tbody tr:not(.skeleton-row)")).size();
	}

	public NavBarComponent navBar() {
		return new NavBarComponent(driver);
	}
}
