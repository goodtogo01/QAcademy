package com.qacademy.automation.data;

/**
 * Seeded demo user roles with their credentials used across test data.
 */
public enum UserRole {
    ADMIN("admin", "Admin123!"),
    STAFF("staff", "Admin123!"),
    STUDENT("student", "Admin123!");
	
	private final String username;
	private final String password;
	
	UserRole(String username, String password) {
		this.username=username;
		this.password=password;
	}
	
	public String getUserName() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
}
