package com.qacademy.automation.api.clients;

import java.util.Map;

import com.qacademy.automation.core.api.BaseApiClient;

import io.restassured.response.Response;

/**
 * API client for course CRUD endpoints.
 */
public class CourseApiClient extends BaseApiClient{

	private static final String COURST_PATH= "/api/course";
	
    /** Open endpoint - no token required. */
	public Response getAllCourse() {
		return get(COURST_PATH);
	}
	
	/** Requires an Admin token - Staff accounts cannot create courses. */
	public Response createCourse(String name, int credits, String token) {
		Map<String, Object> body = Map.of(
				"name", name,
				"credits", credits);
		return post(COURST_PATH, token);
	}
	
}
