package com.qacademy.automation.api.models.request;

/**
 * Request payload for the register endpoint.
 
 record is a class-level keyword (same category as class, interface, enum) — never a method. 
 The parameters in its header are called "record components," 
 and they simultaneously define the fields, the constructor's parameter list, 
 and the accessor method names, all in one declaration.
 
 */
public record RegisterRequest (String username, String password, String role) {

}
