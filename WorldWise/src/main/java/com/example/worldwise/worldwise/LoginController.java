package com.example.worldwise.worldwise;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private final UserDAO userDAO = UserDAO.getInstance();

    private HomeController homeController;

    // Setter to receive HomeController reference
    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter email and password.");
            return;
        }

        boolean valid = userDAO.validateLogin(email, password);

        if (valid) {
            messageLabel.setText("Login successful!");

            if (homeController != null) {
                homeController.onUserLogin();
                try {
                    homeController.showHome(); // optionally navigate to home page
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else {
            messageLabel.setText("Invalid email or password.");
        }
    }

    @FXML
    private void goToRegister() throws IOException {
        if (homeController != null) {
            homeController.showRegisterPage();
        }
    }
}