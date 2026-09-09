package com.qacademy.automation.ui.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.qacademy.automation.ui.pages.BasePage;
import com.qacademy.automation.ui.pages.CoursesPage;
import com.qacademy.automation.ui.pages.EnrollmentsPage;
import com.qacademy.automation.ui.pages.HomePage;
import com.qacademy.automation.ui.pages.LoginPage;
import com.qacademy.automation.ui.pages.StudentsPage;

/**
 * The top navigation bar component, reused by every page after login.
 */
public class NavBarComponent extends BasePage {

    private final By homeLink = By.cssSelector(".topnav a[data-route='home']");
    private final By studentsLink = By.cssSelector(".topnav a[data-route='students']");
    private final By coursesLink = By.cssSelector(".topnav a[data-route='courses']");
    private final By enrollmentsLink = By.cssSelector(".topnav a[data-route='enrollments']");
    private final By usernameDisplay = By.id("username-display");
    private final By roleBadge = By.id("role-badge");
    private final By logoutButton = By.id("logout-btn");

    public NavBarComponent(WebDriver driver) {
        super(driver);
    }

    public HomePage goToHome() {
        click(homeLink);
        return new HomePage(driver);
    }

    public StudentsPage goToStudents() {
        click(studentsLink);
        return new StudentsPage(driver);
    }

    public CoursesPage goToCourses() {
        click(coursesLink);
        return new CoursesPage(driver);
    }

    public EnrollmentsPage goToEnrollments() {
        click(enrollmentsLink);
        return new EnrollmentsPage(driver);
    }

    public LoginPage logout() {
        click(logoutButton);
        return new LoginPage(driver);
    }

    public String getLoggedInUsername() {
        return getText(usernameDisplay);
    }

    public String getRole() {
        return getText(roleBadge);
    }
}
