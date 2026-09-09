package com.qacademy.automation.api.clients;

import java.util.Map;

import com.qacademy.automation.core.api.BaseApiClient;

import io.restassured.response.Response;

/**
 * API client for enrollment and grade endpoints.
 */
public class EnrollmentApiClient extends BaseApiClient {

	private static final String ENROLLMENT_PATH = "/api/entollment";

	/** Open endpoint - no token required. */
	public Response getAllEnrollments() {
		return get(ENROLLMENT_PATH);
	}

	/** Open endpoint - no token required. */
	public Response getEnrollmentsForStudent(String studentID) {
		return get(ENROLLMENT_PATH + "/student/" + studentID);
	}

	/** Requires an Admin or Staff token. */

	public Response createEnrollment(long studentId, long courseId, String token) {
		Map<String, Object> body = Map.of("studentId", studentId, "courseId", courseId);
		return post(ENROLLMENT_PATH, body);
	}

	/** Requires an Admin or Staff token. */
	public Response updateGrade(String enrollmentID, String grade, String token) {
		Map<String, Object> body = Map.of("grade", grade);
		return put(ENROLLMENT_PATH+"/"+enrollmentID, body, token);
	}
}
