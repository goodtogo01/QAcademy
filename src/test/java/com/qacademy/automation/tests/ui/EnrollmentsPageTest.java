package com.qacademy.automation.tests.ui;

import java.time.Duration;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.qacademy.automation.api.clients.AuthApiClient;
import com.qacademy.automation.api.clients.CourseApiClient;
import com.qacademy.automation.api.clients.EnrollmentApiClient;
import com.qacademy.automation.api.clients.StudentApiClient;
import com.qacademy.automation.api.models.request.CourseCreateRequest;
import com.qacademy.automation.api.models.request.StudentCreateRequest;
import com.qacademy.automation.config.ConfigManager;
import com.qacademy.automation.core.base.BaseUiTest;
import com.qacademy.automation.data.TestDataFactory;
import com.qacademy.automation.data.UserRole;
import com.qacademy.automation.ui.pages.EnrollmentsPage;
import com.qacademy.automation.ui.pages.HomePage;
import com.qacademy.automation.ui.pages.LoginPage;

/**
 * UI test class for Enrollments page CRUD. Student/course prerequisites are seeded
 * via the API clients rather than the UI - it's faster and gives us the real numeric
 * IDs the enrollment form's <select> options are keyed on.
 */


public class EnrollmentsPageTest  extends BaseUiTest{
	
	private HomePage homePage;
	private String adminToken;

	// Set as a side effect of seedStudent()/seedCourse() below, so a test method that
	// needs the actual generated name/email (not just the id) - see
	// enrollStudent_withValidStudentAndCourse_addRow() - doesn't need its own copy of
	// TestDataFactory logic.
	private StudentCreateRequest lastStudentData;
	private CourseCreateRequest lastCourseData;
	
    @BeforeMethod(alwaysRun = true)
    public void loginAsAdmin() {
        getDriver().get(ConfigManager.getInstance().getBaseUrl());
        homePage = new LoginPage(getDriver()).login(UserRole.ADMIN.getUserName(), UserRole.ADMIN.getPassword());
        adminToken = new AuthApiClient().loginAndGetToken(UserRole.ADMIN.getUserName(), UserRole.ADMIN.getPassword());
        
    }
	private long seedStudent() {
		lastStudentData = TestDataFactory.validStudent();
		return new StudentApiClient()
				.createStudent(lastStudentData.firstName(), lastStudentData.lastName(), lastStudentData.email(),
						lastStudentData.dateOfBirth(), adminToken)
				.jsonPath().getLong("id");
	}
    private long seedCourse() {
    	lastCourseData = TestDataFactory.validCourse();
    	return new CourseApiClient().createCourse(lastCourseData.name(), lastCourseData.credits(), adminToken).jsonPath().getLong("id");
    }

    @Test
    public void enrollStudent_withValidStudentAndCourse_addRow() {
    	long studentId = seedStudent();
    	String studentFullName = lastStudentData.firstName() + " " + lastStudentData.lastName();
    	long courseId = seedCourse();
    	String courseName = lastCourseData.name();
    	
    	// Seed first, then open the page - the enrollment form's dropdowns are populated
        // from a fresh fetch, so the newly-created student/course need to already exist.
    	
    	
    	EnrollmentsPage enrollmentsPage = homePage.navBar().goToEnrollments();
    	
    	enrollmentsPage.enrollStudent(String.valueOf(studentId), String.valueOf(courseId));
    	
    	// Identity-based, not a row-count delta: the "All enrollments" table is never
    	// reset between suite runs, so a plain getRowCount()==before+1 check has to race
    	// an ever-growing, shared list (this is exactly what made this one test flaky -
    	// see EnrollmentsPage.isEnrollmentPresentByNames()). The random suffix
    	// TestDataFactory gives every seeded name makes this pairing unique, so it only
    	// has to find its own row. isEnrollmentPresentByNames() already waits up to the
    	// explicit-wait timeout internally (same as isEnrollmentRowPresent() used below in
    	// deleteEnrollment_removesRow) - no extra WebDriverWait wrapper needed here.
    	Assert.assertTrue(enrollmentsPage.isEnrollmentPresentByNames(studentFullName, courseName),
    			"Expected a new enrollment row for " + studentFullName + " / " + courseName + " to appear");
    			
    }
    
    @Test
    public void setGrade_updatesGradeBadge() {
    	long studentId = seedStudent();
    	long courseId = seedCourse();
    	long enrollmentId = new EnrollmentApiClient().createEnrollment(studentId, courseId, adminToken)
    			.jsonPath().getLong("id");
    	
    	
    	EnrollmentsPage enrollmentsPage = homePage.navBar().goToEnrollments();
    	Assert.assertEquals(enrollmentsPage.getGrade(String.valueOf(enrollmentId)), "Ungraded");
    	
    	enrollmentsPage.setGrade(String.valueOf(enrollmentId), "A");

        new WebDriverWait(getDriver(), Duration.ofSeconds(ConfigManager.getInstance().getExplicitWaitSeconds()))
                .until(driver -> "A".equals(enrollmentsPage.getGrade(String.valueOf(enrollmentId))));
    }
    
    // Only have full permission to Admin 
    @Test
    public void deleteEnrollment_removesRow() {
        long studentId = seedStudent();
        long courseId = seedCourse();
        long enrollmentId = new EnrollmentApiClient().createEnrollment(studentId, courseId, adminToken)
                .jsonPath().getLong("id");
        
        EnrollmentsPage enrollmentsPage = homePage.navBar().goToEnrollments();
        Assert.assertTrue(enrollmentsPage.isEnrollmentRowPresent(String.valueOf(enrollmentId)));
        
        enrollmentsPage.deleteEnrollment(String.valueOf(enrollmentId));
        
        new WebDriverWait(getDriver(), Duration.ofSeconds(ConfigManager.getInstance().getExplicitWaitSeconds()))
        .until(driver -> !enrollmentsPage.isEnrollmentRowPresent(String.valueOf(enrollmentId)));
        
        
    }
    }
