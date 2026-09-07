package com.qacademy.core.dto;

// Same tuple-with-payload pattern as AuthResult - creating an enrollment can fail
// for a reason a plain boolean can't express (student not found vs. course not
// found), so the caller needs the error message, not just a yes/no.
public record EnrollmentCreateResult(boolean success, String error, EnrollmentDto enrollment) {
}
