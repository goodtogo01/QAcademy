package com.qacademy.automation.api.clients;

import java.util.Map;

import com.qacademy.automation.core.api.ApiEndpoints;
import com.qacademy.automation.core.api.BaseApiClient;

import io.restassured.response.Response;

/**
 * API client for course CRUD endpoints.
 */
public class CourseApiClient extends BaseApiClient {

    /** Open endpoint - no token required. */
	public Response getAllCourses() {
		return get(ApiEndpoints.COURSE);
	}

	/** Requires an Admin token - Staff accounts cannot create courses. */
	public Response createCourse(String name, int credits, String token) {
		Map<String, Object> body = Map.of(
				"name", name,
				"credits", credits);
		return post(ApiEndpoints.COURSE, body, token);
	}

}
