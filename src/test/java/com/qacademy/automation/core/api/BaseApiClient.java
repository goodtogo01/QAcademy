package com.qacademy.automation.core.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Common given()/when() plumbing that every API client extends.
 *
 * Each concrete client (AuthApiClient, StudentApiClient, ...) only needs to
 * know its own endpoint paths and payload types; the actual RestAssured
 * given()/when() calls - and the one shared RequestSpecification from
 * RequestSpecFactory - live here, once.
 */

public abstract class BaseApiClient {

	protected RequestSpecification requestSpec() {
		return RestAssured.given().spec(RequestSpecFactory.getRequestSpec());
	}

	protected RequestSpecification authorizedRequestSpec(String bearerToken) {
		return requestSpec().header("Authorization", "Bearer " + bearerToken);
	}

	protected Response get(String path) {
		return requestSpec().when().get(path);
	}

	protected Response get(String path, String bearerToken) {
		return authorizedRequestSpec(bearerToken).when().get(path);
	}

	protected Response post(String path, Object body) {
		return requestSpec().body(body).when().post(path);
	}
	
	protected Response post(String path, Object body, String bearerToken) {
		return authorizedRequestSpec(bearerToken).body(body).when().post(path);
	}
	protected Response put(String path, Object body, String bearerToken) {
		return authorizedRequestSpec(bearerToken).body(body).when().put(path);
	}
	protected Response delete(String path, String bearerToken) {
		return authorizedRequestSpec(bearerToken).when().delete(path);
	}
	
}
