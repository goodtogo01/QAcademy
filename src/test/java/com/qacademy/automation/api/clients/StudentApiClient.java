package com.qacademy.automation.api.clients;

import java.util.Map;

import com.qacademy.automation.core.api.BaseApiClient;

import io.restassured.response.Response;

/**
 * API client for student CRUD endpoints.
 */
public class StudentApiClient extends BaseApiClient{
	private static final String STUDENT_PATH = "/api/student";
	
    /** Open endpoint - no token required. */
	public Response getAllStudents() {
		return get(STUDENT_PATH);
	}
	
	 /** Requires an Admin or Staff token. */
	public Response createStudent(String firstName, String lastName, String email, String dateOfBirth, String token) {
		Map<String, Object> body = Map.of(
				"firstName", firstName, 
				"lastName", lastName,
				"email", email,
				"dateOfBirth", dateOfBirth );
		return post(STUDENT_PATH, body, token);
	}
	 /** Requires an Admin token. */
	public Response deleteStudent(String studentId, String token) {
		return delete(STUDENT_PATH+ "/"+studentId, token);
	}
}
