package com.qacademy.automation.tests.ui;

import java.time.Duration;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.qacademy.automation.api.clients.AuthApiClient;
import com.qacademy.automation.api.clients.StudentApiClient;
import com.qacademy.automation.api.models.request.StudentCreateRequest;
import com.qacademy.automation.config.ConfigManager;
import com.qacademy.automation.core.base.BaseUiTest;
import com.qacademy.automation.data.TestDataFactory;
import com.qacademy.automation.data.UserRole;
import com.qacademy.automation.ui.pages.HomePage;
import com.qacademy.automation.ui.pages.LoginPage;
import com.qacademy.automation.ui.pages.StudentsPage;

/**
 * UI test class for Students page CRUD.
 */
public class StudentsPageTest extends BaseUiTest {

	private StudentsPage studentsPage; 
	
	@BeforeMethod(alwaysRun = true)
	public void loginAsAdminAndOpenStudentsPage() {
		getDriver().get(ConfigManager.getInstance().getBaseUrl());
		
		HomePage homePage = new LoginPage(getDriver()).login(UserRole.ADMIN.getUserName(), UserRole.ADMIN.getPassword());
		studentsPage = homePage.navBar().goToStudents();		
	}
	
	private void waitUntilRowCountIs(int expected) {
		new WebDriverWait(getDriver(), Duration.ofSeconds(ConfigManager.getInstance().getExplicitWaitSeconds()))
		.until(driver -> studentsPage.getRowCount() == expected);
	}
	
	@Test
	public void addStudent_withValidData_addsRow() {
		int before = studentsPage.getRowCount();
		StudentCreateRequest data = TestDataFactory.validStudent();
		studentsPage.addStudent(data.firstName(), data.lastName(), data.email(), data.dateOfBirth());
		waitUntilRowCountIs(before + 1);
	}
	@Test
	public void addStudent_withInvalidDateOfBirth_showsCalendarErrorAndDoesNotAddRow() {
		int before = studentsPage.getRowCount();
		StudentCreateRequest data = TestDataFactory.studentWithInvalidDateOfBirth();
		
		studentsPage.addStudent(data.firstName(), data.lastName(), data.email(), data.dateOfBirth());
		
		 Assert.assertEquals(studentsPage.getFormError(),
	                "\"" + data.dateOfBirth() + "\" is not a real calendar date - check the day and month aren't swapped.");
	        Assert.assertEquals(studentsPage.getRowCount(), before, "An invalid DOB must not create a student.");
		
	}
	
	 @Test
	    public void deleteStudent_removesRow() {
	       String token = new  AuthApiClient().loginAndGetToken(UserRole.ADMIN.getUserName(), UserRole.ADMIN.getPassword());
	       StudentCreateRequest data = TestDataFactory.validStudent();
	       long studentId = new StudentApiClient()
	    		   .createStudent(data.firstName(), data.lastName(), data.email(), data.dateOfBirth(), token)
	    		   .jsonPath().getLong("id");
	       
	       // Re-open the page so the student seeded via the API (not the UI) shows up in the table.
	       // A plain goToStudents() click won't do it here: we're already on the Students
	       // page (BaseUiTest's @BeforeMethod put us there), and clicking a nav link whose
	       // route is the one we're already on doesn't change window.location.hash - so the
	       // SPA's hashchange-driven router never re-fires and the table never re-fetches.
	       // An explicit browser refresh forces the app to reboot and re-render this route
	       // with fresh data (the login session survives via sessionStorage).
	       getDriver().navigate().refresh();
	       studentsPage = new StudentsPage(getDriver());
	       Assert.assertTrue(studentsPage.isStudentRowPresent(String.valueOf(studentId)));
	       studentsPage.deleteStudent(String.valueOf(studentId));
	       
	       new WebDriverWait(getDriver(), Duration.ofSeconds(ConfigManager.getInstance().getExplicitWaitSeconds()))
	       .until(driver -> !studentsPage.isStudentRowPresent(String.valueOf(studentId)));
	       
	       
	    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
