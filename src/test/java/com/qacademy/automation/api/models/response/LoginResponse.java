package com.qacademy.automation.api.models.response;

/**
 * Response payload from the login endpoint.
 */
public record  LoginResponse (String token, String expiresAtUtc) {

}
