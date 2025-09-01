package com.example.worldwise.worldwise;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML
    private StackPane contentArea;

    // Navbar buttons
    @FXML private Button accountBtn, registerBtn, loginBtn, logoutBtn, profileBtn;

    private boolean loggedIn = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateNavbar();
        try {
            showHome(); // load HomePage.fxml into contentArea
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onUserLogin() {
        loggedIn = true;
        updateNavbar();
    }

    public void onUserLogout() {
        loggedIn = false;
        updateNavbar();
    }

    private void updateNavbar() {
        loginBtn.setVisible(!loggedIn);
        registerBtn.setVisible(!loggedIn);
        logoutBtn.setVisible(loggedIn);
        profileBtn.setVisible(loggedIn);
    }

    private void loadPage(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        Parent page = loader.load();

        contentArea.getChildren().setAll(page);
    }

    @FXML
    void showHome() throws IOException {
        loadPage("HomePage.fxml");
    }

    @FXML
    private void showMapMode() throws IOException {
        loadPage("MapModePage.fxml");
    }

    @FXML
    private void showFlashcardMode() throws IOException {
        loadPage("FlashcardPage.fxml");
    }

    @FXML
    private void showLearnMode() throws IOException {
        loadPage("LearnPage.fxml");
    }

    @FXML
    private void showUserAccount() throws IOException {
        loadPage("UserAccountPage.fxml");
    }

    @FXML
    public void showRegisterPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Register.fxml"));
        Parent page = loader.load();

        // Get controller and pass HomeController reference
        RegisterController controller = loader.getController();
        controller.setHomeController(this);

        contentArea.getChildren().setAll(page);
    }

    @FXML
    public void showLoginPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Parent page = loader.load();

        // Get controller and pass HomeController reference
        LoginController controller = loader.getController();
        controller.setHomeController(this);

        contentArea.getChildren().setAll(page);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        onUserLogout(); // update navbar
        try {
            showHome(); // optionally redirect to home page after logout
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}