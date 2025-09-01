package com.example.worldwise.worldwise;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField firstnameField;

    @FXML
    private TextField lastnameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    private final UserDAO userDAO = UserDAO.getInstance(); // singleton instance

    @FXML
    private void handleRegister() {
        String firstname = firstnameField.getText().trim();
        String lastname = lastnameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();

        if (firstname.isEmpty() || lastname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill in all fields.");
            return;
        }

        if (!isValidEmail(email)) {
            messageLabel.setText("Please enter a valid email address.");
            return;
        }

        if (!password.equals(confirm)) {
            messageLabel.setText("Passwords do not match!");
            return;
        }

        User user = new User(firstname, lastname, email, password);
        boolean success = userDAO.registerUser(user);

        if (success) {
            messageLabel.setText("Registration successful!");
        } else {
            messageLabel.setText("User already exists.");
        }
    }

    // Email validation helper
    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    private HomeController homeController;

    // Setter to receive HomeController reference
    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    @FXML
    private void goToLogin() throws IOException {
        if (homeController != null) {
            homeController.showLoginPage();
        }
    }
}