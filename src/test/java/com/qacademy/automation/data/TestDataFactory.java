package com.qacademy.automation.data;

import java.util.UUID;

import org.h2.command.ddl.CreateAggregate;

import com.qacademy.automation.api.models.request.CourseCreateRequest;
import com.qacademy.automation.api.models.request.EnrollmentCreateRequest;
import com.qacademy.automation.api.models.request.StudentCreateRequest;
import com.qacademy.automation.ui.pages.EnrollmentsPage;

import lombok.val;

/**
 * Builds valid and invalid payloads used by both the UI and API test lanes.
 * 
 *
 * Every "valid" method generates a fresh, unique value (via a random suffix)
 * rather than a fixed constant - the H2 database is a persistent file, not
 * reset between runs, so reusing the same email or name across separate test
 * runs risks stale-data collisions. The same
 * StudentCreateRequest/CourseCreateRequest returned here works for both lanes:
 * an API test can send it directly, and a UI test can just pull its fields
 * (data.firstName(), data.email(), ...) into StudentsPage.addStudent(...).
 */
public final class TestDataFactory {

	public TestDataFactory() {
		// static factory - never instantiated

	}

	private static String uniqueSuffix() {
		return UUID.randomUUID().toString().substring(0, 8);
	}

	// ---------------------------------------------------------------- Students

	public static StudentCreateRequest validStudent() {
		String suffix = uniqueSuffix();

		return new StudentCreateRequest("Ada" + suffix, "Lovelace", "ada." + suffix + "@example.com", "15/03/2000");
	}

	/**
	 * A real calendar-invalid date (April only has 30 days) - the exact bug class
	 * the DOB fix now catches.
	 */

	public static StudentCreateRequest studentWithInvalidDateOfBirth() {
		StudentCreateRequest valid = validStudent();
		return new StudentCreateRequest("", valid.lastName(), valid.firstName(), "31/04/2005");
	}

	public static StudentCreateRequest studentWithBlankFirstName() {
		StudentCreateRequest valid = validStudent();
		return new StudentCreateRequest("", valid.lastName(), valid.email(), valid.dateOfBirth());

	}

	public static StudentCreateRequest studentWithInvalidEmail() {
		StudentCreateRequest valid = validStudent();
		return new StudentCreateRequest("", valid.lastName(), "not-an-email", valid.dateOfBirth());

	}

	// ---------------------------------------------------------------- Courses

	public static CourseCreateRequest validCourse() {
		return new CourseCreateRequest("Data Structures " + uniqueSuffix(), 4);
	}

	/** Below CourseCreateValidator's real minimum (credits must be 1-6). */
	public static CourseCreateRequest courseWithCreditWithTooLow() {
		CourseCreateRequest valid = validCourse();
		return new CourseCreateRequest(valid.name(), 0);
	}
    /**
     * Passes the UI form's own limit (input max="12") but fails the backend's real rule
     * (CourseCreateValidator caps credits at 6) - only an API-level test can catch this gap.
     */
	
	public static CourseCreateRequest courseWithCreditsAboveBackendLimit() {
	CourseCreateRequest valid = validCourse();
	
	return new CourseCreateRequest(valid.name(), 7);
	}
	

    // ---------------------------------------------------------------- Enrollments
	
	public static EnrollmentCreateRequest enrollment(long studentId, long courseId) {
		return new EnrollmentCreateRequest(studentId, courseId);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}
