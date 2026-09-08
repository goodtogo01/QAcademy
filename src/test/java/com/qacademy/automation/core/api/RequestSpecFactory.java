package com.qacademy.automation.core.api;

import com.qacademy.automation.config.ConfigManager;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Builds the shared RestAssured RequestSpecification used by every API client.
 *
 * Wraps RestAssured's own Builder (RequestSpecBuilder) so the base URI, default content
 * type, and request/response logging are configured exactly once, in exactly one place -
 * every API client asks this factory for the shared spec instead of repeating
 * given().baseUri(...).contentType(...) in every method it writes.
 */


public final class RequestSpecFactory {
	private static RequestSpecification sharedSpec;
	
	private RequestSpecFactory(){
		   // static factory - never instantiated
	}
	
	public static synchronized RequestSpecification getRequestSpec() {
		if(sharedSpec == null) {
			sharedSpec = new RequestSpecBuilder()
					.setBaseUri(ConfigManager.getInstance().getBaseUrl())
					.setContentType(ContentType.JSON)
					.addFilter(new RequestLoggingFilter())
					.addFilter(new ResponseLoggingFilter())
					.build();
		}
		return sharedSpec;
	}

}
