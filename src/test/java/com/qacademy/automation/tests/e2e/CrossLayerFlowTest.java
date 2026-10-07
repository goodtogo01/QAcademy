package com.qacademy.automation.tests.e2e;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qacademy.automation.api.clients.AuthApiClient;
import com.qacademy.automation.api.clients.CourseApiClient;
import com.qacademy.automation.api.clients.EnrollmentApiClient;
import com.qacademy.automation.api.clients.StudentApiClient;
import com.qacademy.automation.api.models.request.CourseCreateRequest;
import com.qacademy.automation.api.models.request.StudentCreateRequest;
import com.qacademy.automation.api.models.response.CourseResponse;
import com.qacademy.automation.api.models.response.EnrollmentResponse;
import com.qacademy.automation.api.models.response.StudentResponse;
import com.qacademy.automation.config.ConfigManager;
import com.qacademy.automation.core.base.BaseUiTest;
import com.qacademy.automation.data.TestDataFactory;
import com.qacademy.automation.data.UserRole;
import com.qacademy.automation.ui.pages.CoursesPage;
import com.qacademy.automation.ui.pages.EnrollmentsPage;
import com.qacademy.automation.ui.pages.HomePage;
import com.qacademy.automation.ui.pages.LoginPage;
import com.qacademy.automation.ui.pages.RegisterPage;
import com.qacademy.automation.ui.pages.StudentsPage;

import io.restassured.response.Response;

/**
 * Hybrid UI+API scenarios E2E-01 through E2E-05 from the Functional Analysis
 * document.
 */
public class CrossLayerFlowTest extends BaseUiTest {

	private final AuthApiClient authApiClient = new AuthApiClient();
	private final StudentApiClient studentApiClient = new StudentApiClient();
	private final CourseApiClient courseApiClient = new CourseApiClient();
	private final EnrollmentApiClient enrollmentApiClient = new EnrollmentApiClient();

	// get token
	private String tokenFor(String username, String password) {
		return authApiClient.loginAndGetToken(username, password);
	}

	// sort by email
	private long findStudentIdByEmail(String email) {
		StudentResponse[] students = studentApiClient.getAllStudents().as(StudentResponse[].class);
		return Arrays.stream(students).filter(s -> email.equals(s.email())).findFirst()
				.orElseThrow(() -> new AssertionError("No student found with email " + email)).id();
	}

	// sort by name
	private long findCourseIdByName(String name) {
		CourseResponse[] courses = courseApiClient.getAllCourses().as(CourseResponse[].class);
		return Arrays.stream(courses).filter(c -> name.equals(c.name())).findFirst()
				.orElseThrow(() -> new AssertionError("No course found named " + name)).id();
	}

	// sort by Enrollment id
	private long findEnrollmentId(long studentId, long courseId) {
		EnrollmentResponse[] enrollments = enrollmentApiClient.getAllEnrollments().as(EnrollmentResponse[].class);
		return Arrays.stream(enrollments).filter(e -> studentId == e.studentId() && courseId == e.courseId())
				.findFirst().orElseThrow(() -> new AssertionError(
						"No enrollment found for student " + studentId + " / course " + courseId))
				.id();
	}

	/**
	 * E2E-01: a freshly self-registered Staff account can manage Students but not
	 * Courses - at both layers.
	 */
	@Test
	public void e2e01_newlyRegisteredStaff_canManageStudentsButNotCourses() {
		String username = "staff_" + UUID.randomUUID().toString().substring(0, 8);
		String password = "Password1!";

		getDriver().get(ConfigManager.getInstance().getBaseUrl());
		RegisterPage registerPage = new LoginPage(getDriver()).goToRegister();
		registerPage.register(username, password, "Staff");
		Assert.assertEquals(registerPage.getSuccessMessage(), "Account created! You can sign in now.");

		HomePage homePage = new LoginPage(getDriver()).login(username, password);
		Assert.assertEquals(homePage.navBar().getRole(), "STAFF");

		StudentsPage studentsPage = homePage.navBar().goToStudents();

		Assert.assertTrue(studentsPage.isAddStudentFormDisplayed(),
				"A newly self-registered Staff account should be able to add students immediately.");

		CoursesPage coursesPage = studentsPage.navBar().goToCourses();
		Assert.assertFalse(coursesPage.isAddCourseFormDisplayed(),
				"Course management is Admin-only - Staff should see the read-only view.");

		// The UI hiding the form is cosmetic - confirm the server independently rejects
		// it too.
		Response apiResponse = courseApiClient.createCourse("Should Be Rejected", 3, tokenFor(username, password));
		Assert.assertEquals(apiResponse.statusCode(), 403);
	}

	/**
	 * E2E-02: a student created purely through the API must still show up in the UI
	 * roster.
	 */
	@Test
	public void e2e02_studentCreatedViaApi_appearsInUiRosterOnNextLoad() {
		String adminToken = tokenFor(UserRole.ADMIN.getUserName(), UserRole.ADMIN.getPassword());
		StudentCreateRequest data = TestDataFactory.validStudent();
		long studentId = studentApiClient
				.createStudent(data.firstName(), data.lastName(), data.email(), data.dateOfBirth(), adminToken)
				.jsonPath().getLong("id");

		getDriver().get(ConfigManager.getInstance().getBaseUrl());
		HomePage homePage = new LoginPage(getDriver()).login(UserRole.ADMIN.getUserName(),
				UserRole.ADMIN.getPassword());
		StudentsPage studentsPage = homePage.navBar().goToStudents();

		Assert.assertTrue(studentsPage.isStudentRowPresent(String.valueOf(studentId)),
				"A student created purely via the API must still appear in the UI roster - same database, same service layer.");
	}

	/**
	 * E2E-03: Admin creates via UI, Staff enrolls via UI, grade stays Ungraded
	 * until Admin sets it via API.
	 */
	@Test
	public void e2e03_enrollmentGrade_staysUngradedUntilAdminSetsItViaApi() {
		getDriver().get(ConfigManager.getInstance().getBaseUrl());

		HomePage adminHome = new LoginPage(getDriver()).login(UserRole.ADMIN.getUserName(),
				UserRole.ADMIN.getPassword());

		StudentCreateRequest studentData = TestDataFactory.validStudent();
		StudentsPage studentsPage = adminHome.navBar().goToStudents();
		studentsPage.addStudent(studentData.firstName(), studentData.lastName(), studentData.email(),
				studentData.dateOfBirth());

		CourseCreateRequest courseData = TestDataFactory.validCourse();
		CoursesPage coursesPage = studentsPage.navBar().goToCourses();
		coursesPage.addCourse(courseData.name(), String.valueOf(courseData.credits()));

		// Neither addStudent() nor addCourse() hands back the new row's id - resolve
		// both the
		// same way the UI itself would if it needed them, by asking the API for current
		// data.
		long studentId = findStudentIdByEmail(studentData.email());
		long courseId = findCourseIdByName(courseData.name());

		adminHome.navBar().logout();

		HomePage staffHome = new LoginPage(getDriver()).login(UserRole.STAFF.getUserName(),
				UserRole.STAFF.getPassword());
		EnrollmentsPage enrollmentsPage = staffHome.navBar().goToEnrollments();
		enrollmentsPage.enrollStudent(String.valueOf(studentId), String.valueOf(courseId));

		long enrollmentId = findEnrollmentId(studentId, courseId);

		new WebDriverWait(getDriver(), Duration.ofSeconds(ConfigManager.getInstance().getExplicitWaitSeconds()))
				.until(driver -> "Ungraded".equals(enrollmentsPage.getGrade(String.valueOf(enrollmentId))));

		String adminToken = tokenFor(UserRole.ADMIN.getUserName(), UserRole.ADMIN.getPassword());
		enrollmentApiClient.updateGrade(String.valueOf(enrollmentId), "A", adminToken);

		// sessionStorage (not an in-memory JS var) holds the session, and the router
		// re-reads
		// window.location.hash on boot - so a real refresh stays logged in on the same
		// page.
		getDriver().navigate().refresh();
		EnrollmentsPage refreshedEnrollmentsPage = new EnrollmentsPage(getDriver());

		new WebDriverWait(getDriver(), Duration.ofSeconds(ConfigManager.getInstance().getExplicitWaitSeconds()))
				.until(driver -> "A".equals(refreshedEnrollmentsPage.getGrade(String.valueOf(enrollmentId))));
	}

	/**
	 * E2E-04: deleting a Student via the UI must cascade-delete their enrollments
	 * (NFR-05).
	 */
	@Test
	public void e2e04_deletingStudentViaUi_cascadesToDeleteTheirEnrollments() {
		String adminToken = tokenFor(UserRole.ADMIN.getUserName(), UserRole.ADMIN.getPassword());

		StudentCreateRequest studentData = TestDataFactory.validStudent();
		long studentId = studentApiClient.createStudent(studentData.firstName(), studentData.lastName(),
				studentData.email(), studentData.dateOfBirth(), adminToken).jsonPath().getLong("id");
		CourseCreateRequest courseData = TestDataFactory.validCourse();
		long courseId = courseApiClient.createCourse(courseData.name(), courseData.credits(), adminToken).jsonPath()
				.getLong("id");
		enrollmentApiClient.createEnrollment(studentId, courseId, adminToken);

		getDriver().get(ConfigManager.getInstance().getBaseUrl());
		HomePage homePage = new LoginPage(getDriver()).login(UserRole.ADMIN.getUserName(),
				UserRole.ADMIN.getPassword());
		StudentsPage studentsPage = homePage.navBar().goToStudents();
		studentsPage.deleteStudent(String.valueOf(studentId));

		new WebDriverWait(getDriver(), Duration.ofSeconds(ConfigManager.getInstance().getExplicitWaitSeconds()))
				.until(driver -> !studentsPage.isStudentRowPresent(String.valueOf(studentId)));

		EnrollmentResponse[] remaining = enrollmentApiClient.getEnrollmentsForStudent(String.valueOf(studentId))
				.as(EnrollmentResponse[].class);
		Assert.assertEquals(remaining.length, 0, "Deleting a student must cascade-delete their enrollments (NFR-05).");
	}

	/**
	 * E2E-05: the UI hiding a button is cosmetic - the API rejects the same write
	 * independently.
	 */
	@Test
	public void e2e05_serverRejectsUnauthorizedWrite_evenThoughUiNeverRenderedTheButton() {
		getDriver().get(ConfigManager.getInstance().getBaseUrl());
		HomePage homePage = new LoginPage(getDriver()).login(UserRole.STUDENT.getUserName(),
				UserRole.STUDENT.getPassword());
		CoursesPage coursesPage = homePage.navBar().goToCourses();

		Assert.assertFalse(coursesPage.isAddCourseFormDisplayed(),
				"A Student account should never see the add-course form - this is the UI-layer gating.");

		String studentToken = tokenFor(UserRole.STUDENT.getUserName(), UserRole.STUDENT.getPassword());
		Response withStudentToken = courseApiClient.createCourse("Should Be Rejected", 3, studentToken);
		Assert.assertEquals(withStudentToken.statusCode(), 401,
				"The same write, sent directly to the API with a Student's own valid token, must still be rejected.");

		// A genuinely expired token isn't practical to manufacture here (tokens live 2
		// hours per
		// application.properties) - an absent/invalid one exercises the same rejection
		// path.
		Response withNoToken = courseApiClient.createCourse("Should Also Be Rejected", 3, "");
		Assert.assertEquals(withNoToken.statusCode(), 403);
	}

}
