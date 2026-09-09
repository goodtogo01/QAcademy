package com.qacademy.automation.api.clients;

import java.util.Map;

import com.qacademy.automation.core.api.BaseApiClient;

import io.restassured.response.Response;

/**
 * API client for login and registration endpoints.
 */
public class AuthApiClient extends BaseApiClient {
	
	private static final String LOGIN_PATH = "api/auth/login";
	private static final String REGISTER_PATH = "/api/auth/register";
	
	public Response login(String username, String password) {
		Map<String, Object> body = Map.of("username", username, "password", password);
		return post(LOGIN_PATH, body);
	}
	
	public Response register(String username, String password) {
		Map<String, Object> body = Map.of("username", username, "password", password);
		return post(REGISTER_PATH, body);
	}
	/** Logs in and pulls the JWT straight out of the response - 
	    the one thing almost every other client method needs. */
	public String loginAndGetToken(String username, String password) {
		return login(username, password).jsonPath().getString("token");
	}
	
}
