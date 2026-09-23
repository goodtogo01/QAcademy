package com.qacademy.automation.tests.api;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.qacademy.automation.api.clients.AuthApiClient;
import com.qacademy.automation.api.clients.StudentApiClient;
import com.qacademy.automation.api.models.request.StudentCreateRequest;
import com.qacademy.automation.api.models.response.StudentResponse;
import com.qacademy.automation.core.base.BaseApiTest;
import com.qacademy.automation.data.TestDataFactory;
import com.qacademy.automation.data.UserRole;

import io.restassured.response.Response;

/**
 * API test class for /api/student. GET is open to everyone; POST allows Admin or Staff;
 * DELETE is Admin-only - that asymmetry (Staff can create but not delete) is deliberately
 * covered below, since it's easy to assume the two share one rule.
 */


public class StudentApiTest extends BaseApiTest {

	private final AuthApiClient authApiClient = new AuthApiClient();
	private final StudentApiClient studentApiClient = new StudentApiClient();
	
	private String tokenFor(UserRole role) {
		return authApiClient.loginAndGetToken(role.getUserName(), role.getPassword());
		
		}
	private long seedStudent() {
        StudentCreateRequest data = TestDataFactory.validStudent();
        return studentApiClient
                .createStudent(data.firstName(), data.lastName(), data.email(), data.dateOfBirth(), tokenFor(UserRole.ADMIN))
                .jsonPath().getLong("id");
    }
	
	@Test
	public void getAllStudents_isOpenAndReturnsSeedData() {
		Response response = studentApiClient.getAllStudents();
		
		Assert.assertEquals(response.statusCode(), 200);
        
		// Student id 1 (Ada Lovelace) is seeded once by V1__init.sql and never deleted by
        // these tests, so this should never come back empty even on a fresh DB.
		Assert.assertTrue(response.as(StudentResponse[].class).length > 0);
		
	}
	@Test
	public void createStudent_asAdmin_returns201() {
		StudentCreateRequest data = TestDataFactory.validStudent();
		Response response = studentApiClient.createStudent(
				data.firstName(), data.lastName(), data.email(), data.dateOfBirth(), tokenFor(UserRole.ADMIN));
		
		Assert.assertEquals(response.statusCode(), 201);
		Assert.assertNotNull(response.jsonPath().getLong("id"));
		Assert.assertEquals(response.jsonPath().getString("email"), data.email());
	}
	@Test
	public void createStudent_asStaff_returns201() {
		// Admin OR Staff can create students - unlike delete, which is Admin-only (see below).
		  StudentCreateRequest data = TestDataFactory.validStudent();
			Response response = studentApiClient.createStudent(
					data.firstName(), data.lastName(), data.email(), data.dateOfBirth(), tokenFor(UserRole.STAFF));
			Assert.assertEquals(response.statusCode(), 201);  
		
	}
	@Test
	public void createStudent_asStudentRole_isForbidden() {
		  StudentCreateRequest data = TestDataFactory.validStudent();
		  Response response = studentApiClient.createStudent(
	                data.firstName(), data.lastName(), data.email(), data.dateOfBirth(), tokenFor(UserRole.STUDENT));
		  Assert.assertEquals(response.statusCode(), 401);

	}
	@Test
	public void createStudent_withBlankFirstName_returns_400_WithSingleValidationMessage() {
		// This is exactly the case the UI form's native "required" attribute blocks before
        // it ever reaches the server - only the raw API can actually exercise this rule.
		StudentCreateRequest data = TestDataFactory.studentWithBlankFirstName();
		
		Response response = studentApiClient.createStudent(
                data.firstName(), data.lastName(), data.email(), data.dateOfBirth(), tokenFor(UserRole.ADMIN));
        Assert.assertEquals(response.statusCode(), 400);
        Assert.assertEquals(Arrays.asList(response.as(String[].class)), List.of("firstName is required."));
    }
	@Test
	public void createStudent_withInvalidEmail_returns400WithSingleValidationMessage() {
		  // Same story - the form's type="email" input blocks "not-an-email" natively.
		StudentCreateRequest data = TestDataFactory.studentWithInvalidEmail();
		
		Response response = studentApiClient.createStudent(
                data.firstName(), data.lastName(), data.email(), data.dateOfBirth(), tokenFor(UserRole.ADMIN));

        Assert.assertEquals(response.statusCode(), 400);
        Assert.assertEquals(Arrays.asList(response.as(String[].class)),
                List.of("email must be a valid email address."));
	}
	
	@Test
	public void createStudent_withInvalidDateOfBirth_returns_400_WithCalendarErrorMessage() {
		StudentCreateRequest data = TestDataFactory.studentWithInvalidDateOfBirth();

        Response response = studentApiClient.createStudent(
                data.firstName(), data.lastName(), data.email(), data.dateOfBirth(), tokenFor(UserRole.ADMIN));

        Assert.assertEquals(response.statusCode(), 400);
        Assert.assertEquals(Arrays.asList(response.as(String[].class)), List.of(
                "dateOfBirth '" + data.dateOfBirth() + "' is not a real calendar date - check the day and month aren't swapped."));
    
	}
	
	@Test
	public void deleteStudent_asStaff_isForbidden() {
		// Confirms the asymmetry: Staff could create a student above, but cannot delete one.
        long studentId = seedStudent();
        Response response = studentApiClient.deleteStudent(String.valueOf(studentId), tokenFor(UserRole.STAFF));
        Assert.assertEquals(response.statusCode(), 403);
	}
	@Test
	public void deleteStudent_asAdmin_removesStudent() {
		long studentId = seedStudent();
		Response response = studentApiClient.deleteStudent(String.valueOf(studentId), tokenFor(UserRole.ADMIN));

        Assert.assertEquals(response.statusCode(), 204);
	}
	@Test
	public void deleteStudent_withNonExistentId_returns_404_WithEmptyBody() {
		Response response = studentApiClient.deleteStudent("999999999", tokenFor(UserRole.ADMIN));
		Assert.assertEquals(response.statusCode(), 404);
        Assert.assertTrue(response.getBody().asString().isEmpty(),
                "notFound().build() sends no body at all - unlike a validation failure, there's no JSON here.");

	}
	
	
	
	
	
	
	
}
