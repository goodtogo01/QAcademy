package com.qacademy.automation.tests.ui;

import java.time.Duration;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.qacademy.automation.api.models.request.CourseCreateRequest;
import com.qacademy.automation.config.ConfigManager;
import com.qacademy.automation.core.base.BaseUiTest;
import com.qacademy.automation.data.TestDataFactory;
import com.qacademy.automation.data.UserRole;
import com.qacademy.automation.ui.pages.CoursesPage;
import com.qacademy.automation.ui.pages.HomePage;
import com.qacademy.automation.ui.pages.LoginPage;

/**
 * UI test class for Courses page CRUD. Only an Admin account sees the
 * add-course form at all (Staff/Student get a read-only note), so every test
 * here logs in as Admin.
 *
 * courseWithCreditWithTooLow() (credits=0) is NOT used here - the credits input
 * has a native min="1", so the browser blocks submission before it ever reaches
 * our JS.
 */

public class CoursesPageTest extends BaseUiTest {
	private CoursesPage coursesPage;

	@BeforeMethod(alwaysRun = true)
	public void loginAsAdminAndOpenCoursesPage() {
		getDriver().get(ConfigManager.getInstance().getBaseUrl());

		HomePage homePage = new LoginPage(getDriver()).login(UserRole.ADMIN.getUserName(),
				UserRole.ADMIN.getPassword());
		coursesPage = homePage.navBar().goToCourses();
	}
	
	@Test
	public void addCourse_withValidData_addsRow() {
		int before = coursesPage.getRowCount();
		CourseCreateRequest data = TestDataFactory.validCourse();
		
		coursesPage.addCourse(data.name(), String.valueOf(data.credits()));
		
		new WebDriverWait(getDriver(), Duration.ofSeconds(ConfigManager.getInstance().getExplicitWaitSeconds()))
		.until(driver -> coursesPage.getRowCount() == before + 1);
	
	}
	
    /**
     * Documents a real, known gap: the form's own input allows up to 12 credits,
     * but CourseCreateValidator on the backend actually caps at 6. 7 passes the
     * browser's own max="12" check, reaches the server, and comes back rejected -
     * this test pins that mismatch until the UI's max attribute is corrected to match.
     */
	
	@Test
	public void addCourse_withCreditsAboveBackendLimit_showsMismatchErrorAndDoesNotAddRow() {
		int before = coursesPage.getRowCount();
		CourseCreateRequest data = TestDataFactory.courseWithCreditsAboveBackendLimit();
		
		coursesPage.addCourse(data.name(), String.valueOf(data.credits()));
		
		Assert.assertEquals(coursesPage.getFormError(), "credits must be between 1 and 6.");
		Assert.assertEquals(coursesPage.getRowCount(), before, "A backend-rejected credits value must not add a course.");
				
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
