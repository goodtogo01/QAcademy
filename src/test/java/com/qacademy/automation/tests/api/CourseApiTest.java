package com.qacademy.automation.tests.api;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.qacademy.automation.api.clients.AuthApiClient;
import com.qacademy.automation.api.clients.CourseApiClient;
import com.qacademy.automation.api.models.request.CourseCreateRequest;
import com.qacademy.automation.api.models.response.CourseResponse;
import com.qacademy.automation.core.base.BaseApiTest;
import com.qacademy.automation.data.TestDataFactory;
import com.qacademy.automation.data.UserRole;

import io.restassured.response.Response;

/**
 * API test class for /api/course. Unlike Student (Admin OR Staff can create),
 * course creation is Admin-only - Staff gets 401 too. There's also no DELETE
 * route on this controller at all, so there's nothing to test there.
 */

public class CourseApiTest extends BaseApiTest {

	private final AuthApiClient authApiClient = new AuthApiClient();
	private final CourseApiClient courseApiClient = new CourseApiClient();

	// Collect token
	private String tokenFor(UserRole role) {
		return authApiClient.loginAndGetToken(role.getUserName(), role.getPassword());
	}

	@Test
	public void getAllCourses_isOpenAndReturnsSeedData() {
		Response response = courseApiClient.getAllCourses();

		Assert.assertEquals(response.statusCode(), 200);
		// Course ids 1/2 ("Algebra I", "US History") are seeded once by V1__init.sql.
		Assert.assertTrue(response.as(CourseResponse[].class).length > 0);
	}

	@Test
	public void createCourse_asAdmin_returns201() {
		CourseCreateRequest data = TestDataFactory.validCourse();

		Response response = courseApiClient.createCourse(data.name(), data.credits(), tokenFor(UserRole.ADMIN));

		Assert.assertEquals(response.statusCode(), 201);
		Assert.assertNotNull(response.jsonPath().getLong("id"));
	}

	@Test
	public void createCourse_asStaff_isForbidden() {
		// No hasAnyRole here, unlike Student's POST - Staff is denied too, Admin-only.
		CourseCreateRequest data = TestDataFactory.validCourse();

		Response response = courseApiClient.createCourse(data.name(), data.credits(), tokenFor(UserRole.STAFF));

		Assert.assertEquals(response.statusCode(), 401);
	}

	@Test
	public void createCourse_withCreditsBelowMinimum_returns400() {
		// credits=0 can't be reached through the UI - its number input has min="1" -
		// but the
		// raw API has no such gate, so this exercises CourseCreateValidator directly.
		CourseCreateRequest data = TestDataFactory.courseWithCreditWithTooLow();

		Response response = courseApiClient.createCourse(data.name(), data.credits(), tokenFor(UserRole.ADMIN));

		Assert.assertEquals(response.statusCode(), 400);
		Assert.assertEquals(Arrays.asList(response.as(String[].class)), List.of("credits must be between 1 and 6."));
	}

	@Test
	public void createCourse_withCreditsAboveBackendLimit_returns400() {
		// The known UI/backend mismatch: the form's own max="12" would let 7 through
		// the
		// browser, but the real backend rule is 1-6. This is the API-level proof of
		// that gap.
		CourseCreateRequest data = TestDataFactory.courseWithCreditsAboveBackendLimit();

		Response response = courseApiClient.createCourse(data.name(), data.credits(), tokenFor(UserRole.ADMIN));

		Assert.assertEquals(response.statusCode(), 400);
		Assert.assertEquals(Arrays.asList(response.as(String[].class)), List.of("credits must be between 1 and 6."));
	}
}
