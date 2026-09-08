package com.qacademy.automation.core.base;

import com.qacademy.automation.core.api.RequestSpecFactory;

import io.restassured.specification.RequestSpecification;

/**
 * Extends BaseTest; provides the base request spec for every API test.
 */
public abstract class BaseApiTest extends BaseTest{
	protected RequestSpecification getRequestSpec() {
		return RequestSpecFactory.getRequestSpec();
	}

}
