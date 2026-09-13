package com.qacademy.automation.api.models.response;

/**
 * Response payload for a student record.
 */
public record StudentResponse (Long id, String firstName, String lastName, String email, String dateOfBirth){

}
