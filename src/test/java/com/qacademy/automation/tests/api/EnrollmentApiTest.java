package com.qacademy.automation.tests.api;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.qacademy.automation.api.clients.AuthApiClient;
import com.qacademy.automation.api.clients.EnrollmentApiClient;
import com.qacademy.automation.core.base.BaseApiTest;
import com.qacademy.automation.data.UserRole;

import io.restassured.response.Response;

/**
 * API test class for /api/enrollment. POST and PUT (grade) allow Admin or Staff;
 * DELETE is Admin-only. There's no uniqueness constraint at all on (studentId, courseId) -
 * enrolling the same pair twice is allowed by design, so that's not tested as a failure case.
 */

public class EnrollmentApiTest extends BaseApiTest{
	
	/** Seeded once by V1__init.sql on a fresh DB - Ada Lovelace (student) and Algebra I (course). */
    private static final long SEEDED_STUDENT_ID = 1L;
    private static final long SEEDED_COURSE_ID = 1L;

    private final AuthApiClient authApiClient = new AuthApiClient();
    private final EnrollmentApiClient enrollmentApiClient = new EnrollmentApiClient();

    // Collect token
    private String tokenFor(UserRole role) {
        return authApiClient.loginAndGetToken(role.getUserName(), role.getPassword());
    }
    
    // Seed Enrollment
    private long seedEnrollment() {
        return enrollmentApiClient
                .createEnrollment(SEEDED_STUDENT_ID, SEEDED_COURSE_ID, tokenFor(UserRole.ADMIN))
                .jsonPath().getLong("id");
    }
    @Test
    public void getAllEnrollments_isOpen() {
        Response response = enrollmentApiClient.getAllEnrollments();

        Assert.assertEquals(response.statusCode(), 200);
    }
    @Test
    public void createEnrollment_asStaff_returns201WithNoGradeYet() {
        Response response = enrollmentApiClient.createEnrollment(SEEDED_STUDENT_ID, SEEDED_COURSE_ID, tokenFor(UserRole.STAFF));

        Assert.assertEquals(response.statusCode(), 201);
        Assert.assertNotNull(response.jsonPath().getLong("id"));
        // Grade is assigned later via PUT, never at creation time.
        Assert.assertNull(response.jsonPath().get("grade"));
    }
    

    @Test
    public void createEnrollment_asStudentRole_isForbidden() {
        Response response = enrollmentApiClient.createEnrollment(SEEDED_STUDENT_ID, SEEDED_COURSE_ID, tokenFor(UserRole.STUDENT));

        Assert.assertEquals(response.statusCode(), 403);
    }
    @Test
    public void createEnrollment_withNonExistentStudentId_returns400WithMessageObject() {
        Response response = enrollmentApiClient.createEnrollment(999999999L, SEEDED_COURSE_ID, tokenFor(UserRole.ADMIN));

        Assert.assertEquals(response.statusCode(), 400);
        Assert.assertEquals(response.jsonPath().getString("message"), "Student not found");
    }
    
    @Test
    public void createEnrollment_withNonExistentCourseId_returns400WithMessageObject() {
        Response response = enrollmentApiClient.createEnrollment(SEEDED_STUDENT_ID, 999999999L, tokenFor(UserRole.ADMIN));

        Assert.assertEquals(response.statusCode(), 400);
        Assert.assertEquals(response.jsonPath().getString("message"), "Course not found");
    }
    @Test
    public void updateGrade_withValidGrade_returns200() {
        long enrollmentId = seedEnrollment();

        Response response = enrollmentApiClient.updateGrade(String.valueOf(enrollmentId), "A", tokenFor(UserRole.ADMIN));

        Assert.assertEquals(response.statusCode(), 200);
        Assert.assertEquals(response.jsonPath().getString("grade"), "A");
    }
    
    @Test
    public void updateGrade_withInvalidFormat_returns400() {
        long enrollmentId = seedEnrollment();

        Response response = enrollmentApiClient.updateGrade(String.valueOf(enrollmentId), "A+-", tokenFor(UserRole.ADMIN));

        Assert.assertEquals(response.statusCode(), 400);
        Assert.assertEquals(Arrays.asList(response.as(String[].class)),
                List.of("grade must be a letter grade like A, B+, or C- (max one letter plus an optional +/-)."));
    }
    @Test
    public void updateGrade_withNonExistentEnrollmentId_returns404WithEmptyBody() {
        Response response = enrollmentApiClient.updateGrade("999999999", "A", tokenFor(UserRole.ADMIN));

        Assert.assertEquals(response.statusCode(), 404);
        Assert.assertTrue(response.getBody().asString().isEmpty());
    }
    @Test
    public void deleteEnrollment_asStaff_isForbidden() {
        long enrollmentId = seedEnrollment();

        Response response = enrollmentApiClient.deleteEnrollment(String.valueOf(enrollmentId), tokenFor(UserRole.STAFF));

        Assert.assertEquals(response.statusCode(), 403);
    }   
    @Test
    public void deleteEnrollment_asAdmin_removesEnrollment() {
        long enrollmentId = seedEnrollment();

        Response response = enrollmentApiClient.deleteEnrollment(String.valueOf(enrollmentId), tokenFor(UserRole.ADMIN));

        Assert.assertEquals(response.statusCode(), 204);
    }
 
}
