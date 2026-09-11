package com.qacademy.automation.api.clients;

import java.util.Map;

import com.qacademy.automation.core.api.ApiEndpoints;
import com.qacademy.automation.core.api.BaseApiClient;

import io.restassured.response.Response;

/**
 * API client for enrollment and grade endpoints.
 */
public class EnrollmentApiClient extends BaseApiClient {

	/** Open endpoint - no token required. */
	public Response getAllEnrollments() {
		return get(ApiEndpoints.ENROLLMENT);
	}

	/** Open endpoint - no token required. */
	public Response getEnrollmentsForStudent(String studentId) {
		return get(ApiEndpoints.ENROLLMENT + "/student/" + studentId);
	}

	/** Requires an Admin or Staff token. */
	public Response createEnrollment(long studentId, long courseId, String token) {
		Map<String, Object> body = Map.of("studentId", studentId, "courseId", courseId);
		return post(ApiEndpoints.ENROLLMENT, body, token);
	}

	/** Requires an Admin or Staff token. */
	public Response updateGrade(String enrollmentId, String grade, String token) {
		Map<String, Object> body = Map.of("grade", grade);
		return put(ApiEndpoints.ENROLLMENT + "/" + enrollmentId, body, token);
	}
}
