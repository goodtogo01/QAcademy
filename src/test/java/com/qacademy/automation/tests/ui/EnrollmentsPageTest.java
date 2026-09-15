package com.qacademy.automation.tests.ui;

import java.io.ObjectInputFilter.Config;
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
	
    @BeforeMethod(alwaysRun = true)
    public void loginAsAdmin() {
        getDriver().get(ConfigManager.getInstance().getBaseUrl());
        homePage = new LoginPage(getDriver()).login(UserRole.ADMIN.getUserName(), UserRole.ADMIN.getPassword());
        adminToken = new AuthApiClient().loginAndGetToken(UserRole.ADMIN.getUserName(), UserRole.ADMIN.getPassword());
        
    }
	private long seedStudent() {
		StudentCreateRequest data = TestDataFactory.validStudent();
		return new StudentApiClient()
				.createStudent(data.firstName(), data.lastName(), data.email(), data.dateOfBirth(), adminToken)
				.jsonPath().getLong("id");
	}
    private long seedCourse() {
    	CourseCreateRequest data = TestDataFactory.validCourse();
    	return new CourseApiClient().createCourse(data.name(), data.credits(), adminToken).jsonPath().getLong("id");
    }

    @Test
    public void enrollStudent_withValidStudentAndCourse_addRow() {
    	long studentId = seedStudent();
    	long courseId = seedCourse();
    	
    	// Seed first, then open the page - the enrollment form's dropdowns are populated
        // from a fresh fetch, so the newly-created student/course need to already exist.
    	
    	
    	EnrollmentsPage enrollmentsPage = homePage.navBar().goToEnrollments();
    	int before = enrollmentsPage.getRowCount();
    	
    	enrollmentsPage.enrollStudent(String.valueOf(studentId), String.valueOf(courseId));
    	
    	new WebDriverWait(getDriver(), Duration.ofSeconds(ConfigManager.getInstance().getExplicitWaitSeconds()))
    	.until(driver -> enrollmentsPage.getRowCount() == before + 1);
    			
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
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

