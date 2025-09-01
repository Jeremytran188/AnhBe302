package com.example.worldwise.worldwise;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDAOTest {

    private UserDAO userDAO;

    @BeforeAll
    void setupDatabase() throws SQLException {
        // Initialize DB
        Database.initializeDatabase();
        userDAO = UserDAO.getInstance();

        // Clean up table before tests
        try (Connection conn = Database.connect(); Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM users");
        }
    }

    @Test
    void testRegisterUserSuccess() {
        User user = new User("John", "Doe", "john@example.com", "password123");

        boolean result = userDAO.registerUser(user);
        assertTrue(result, "User registration should succeed");
    }

    @Test
    void testRegisterUserDuplicateEmail() {
        User user1 = new User("Jane", "Doe", "jane@example.com", "password123");
        User user2 = new User("Jane2", "Doe2", "jane@example.com", "password456");

        assertTrue(userDAO.registerUser(user1), "First registration should succeed");
        assertFalse(userDAO.registerUser(user2), "Duplicate email registration should fail");
    }

    @Test
    void testValidateLoginSuccess() {
        User user = new User("Alice", "Smith", "alice@example.com", "mypassword");
        userDAO.registerUser(user);

        boolean loginResult = userDAO.validateLogin("alice@example.com", "mypassword");
        assertTrue(loginResult, "Login should succeed with correct credentials");
    }

    @Test
    void testValidateLoginWrongPassword() {
        User user = new User("Bob", "Brown", "bob@example.com", "secret");
        userDAO.registerUser(user);

        boolean loginResult = userDAO.validateLogin("bob@example.com", "wrongpassword");
        assertFalse(loginResult, "Login should fail with incorrect password");
    }

    @Test
    void testValidateLoginNonExistentUser() {
        boolean loginResult = userDAO.validateLogin("nonexistent@example.com", "password");
        assertFalse(loginResult, "Login should fail for non-existent user");
    }

    @Test
    void testRegisterUserEmptyFields() {
        User emptyUser = new User("", "", "", "");
        boolean result = userDAO.registerUser(emptyUser);
        assertFalse(result, "Registration should fail with empty fields");
    }

    @Test
    void testRegisterUserNullFields() {
        User nullUser = new User(null, null, null, null);
        boolean result = userDAO.registerUser(nullUser);
        assertFalse(result, "Registration should fail with null fields");
    }

    @Test
    void testRegisterUserInvalidEmail() {
        User invalidEmailUser = new User("Tom", "Hardy", "invalid-email", "password");
        boolean result = userDAO.registerUser(invalidEmailUser);
        assertFalse(result, "Registration should fail with invalid email format");
    }
    @Test
    void testLoginEmptyEmail() {
        boolean result = userDAO.validateLogin("", "password");
        assertFalse(result, "Login should fail with empty email");
    }

    @Test
    void testLoginEmptyPassword() {
        boolean result = userDAO.validateLogin("john@example.com", "");
        assertFalse(result, "Login should fail with empty password");
    }

    @Test
    void testLoginNullEmailPassword() {
        boolean result = userDAO.validateLogin(null, null);
        assertFalse(result, "Login should fail with null email and password");
    }
}