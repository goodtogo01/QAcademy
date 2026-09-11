package com.qacademy.automation.api.clients;

import java.util.Map;

import com.qacademy.automation.core.api.ApiEndpoints;
import com.qacademy.automation.core.api.BaseApiClient;

import io.restassured.response.Response;

/**
 * API client for login and registration endpoints.
 */
public class AuthApiClient extends BaseApiClient {

	public Response login(String username, String password) {
		Map<String, Object> body = Map.of("username", username, "password", password);
		return post(ApiEndpoints.LOGIN, body);
	}

	public Response register(String username, String password, String role) {
		Map<String, Object> body = Map.of("username", username, "password", password, "role", role);
		return post(ApiEndpoints.REGISTER, body);
	}

	/** Logs in and pulls the JWT straight out of the response -
	    the one thing almost every other client method needs. */
	public String loginAndGetToken(String username, String password) {
		return login(username, password).jsonPath().getString("token");
	}

}
