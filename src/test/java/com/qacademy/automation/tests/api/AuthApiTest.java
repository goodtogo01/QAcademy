package com.qacademy.automation.tests.api;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.qacademy.automation.api.clients.AuthApiClient;
import com.qacademy.automation.core.base.BaseApiTest;
import com.qacademy.automation.data.UserRole;

import io.restassured.response.Response;

/**
 * API test class for /api/auth/register and /api/auth/login.
 *
 * Two different 400 shapes on purpose: RegisterRequestValidator failures come
 * back as a plain top-level JSON array of strings, but a duplicate username
 * (which passes DTO validation and fails in the service layer instead) comes
 * back as {"message": "..."}.
 */

public class AuthApiTest extends BaseApiTest {
	private final AuthApiClient authApiClient = new AuthApiClient();

	private String uniqueUsername() {
		return "user_" + UUID.randomUUID().toString().substring(0, 8);
	}

	@Test
	public void register_withValidData_createsUser() {
		Response response = authApiClient.register(uniqueUsername(), "Password1!", "Student");

		Assert.assertEquals(response.statusCode(), 200);
		Assert.assertEquals(response.jsonPath().getString("message"), "User created successfully");
	}
	@Test
	public void register_isCaseInsensitiveOnRole () {
		
	
	
	// AuthServiceImpl does UserRole.valueOf(role.toUpperCase()), and the validator checks
    // with equalsIgnoreCase - lowercase "student" must succeed exactly like "Student".
	
	Response response = authApiClient.register(uniqueUsername(), "Password1!", "student");
	Assert.assertEquals(response.statusCode(), 200);
	
	}
	
	@Test
	public void register_withShortPassword_returns_400_withValidationArray() {
		Response response = authApiClient.register(uniqueUsername(), "short", "Student");
		
		Assert.assertEquals(response.statusCode(), 400);
		Assert.assertEquals(Arrays.asList(response.as(String[].class)), 
				List.of("password must be at least 8 characters."));
	}
  
	@Test
	public void register_withInvalidRole_returns_400_withValidationArray() {
		Response response = authApiClient.register(uniqueUsername(), "Password1!", "SuperAdmin");
		
		Assert.assertEquals(response.statusCode(), 400);
		Assert.assertEquals(Arrays.asList(response.as(String[].class)), 
				List.of("role must be Staff or Student."));
	}

	/**
	 * Security regression test: /api/auth/register is a permitAll endpoint (no token
	 * needed), so "Admin" must NOT be an acceptable role here - otherwise any anonymous
	 * caller could grant themselves a full admin account. The one admin account this app
	 * ships with is created by DataSeeder at startup; self-registration only ever
	 * produces Staff or Student. Pins the fix in RegisterRequestValidator/AuthServiceImpl.
	 */
	@Test
	public void register_withAdminRole_isRejected_cannotSelfProvisionAdmin() {
		Response response = authApiClient.register(uniqueUsername(), "Password1!", "Admin");

		Assert.assertEquals(response.statusCode(), 400);
		Assert.assertEquals(Arrays.asList(response.as(String[].class)),
				List.of("role must be Staff or Student."));
	}
	
	@Test
	public void login_withValidCredentials_returnsTokenAndExpiry() {
		Response response = authApiClient.login(UserRole.ADMIN.getUserName(), UserRole.ADMIN.getPassword());
		
		Assert.assertEquals(response.statusCode(), 200);
		Assert.assertNotNull(response.jsonPath().getString("token"));
		Assert.assertNotNull(response.jsonPath().getString("expiresAtUtc"));
	}
	
	@Test
	public void login_withWrongPassword_returns_401() {
		Response response = authApiClient.login(UserRole.ADMIN.getUserName(), "WrongPasswotd");
		
		Assert.assertEquals(response.statusCode(), 401);
		Assert.assertEquals(response.jsonPath().getString("message"), "Invalid username or password");
		
	}
	
	@Test
	public void login_withUnknownUsername_returnsSameMessageAsWrongPassword() {
		
		// AuthServiceImpl.login returns Optional.empty() for both an unknown username and a
        // wrong password - same status, same message, on purpose: the API never reveals which one was wrong.
		Response response = authApiClient.login("no-such-user-"+UUID.randomUUID(), "Whatever1!");
		
		Assert.assertEquals(response.statusCode(), 401);
		Assert.assertEquals(response.jsonPath().getString("message"), "Invalid username or password");
      		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
