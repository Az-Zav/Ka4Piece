package com.ka4piece.controller;

import com.ka4piece.app.App;
import com.ka4piece.controller.LogoutModalController;
import com.ka4piece.model.Session;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class NavigationUtils {

    /**
     * Opens the custom confirmation logout modal window.
     * Clears active user session and redirects to login screen upon confirmation.
     */
    public static void showLogoutModal(Event event) {
        try {
            URL resource = NavigationUtils.class.getResource("/view/logout_modal.fxml");
            if (resource == null) {
                resource = NavigationUtils.class.getResource("/logout_modal.fxml");
            }

            if (resource == null) {
                System.err.println("Logout modal FXML file not found.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent modalRoot = loader.load();

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            modalStage.setTitle("Confirm Logout");
            modalStage.setScene(new Scene(modalRoot));
            modalStage.setResizable(false);
            modalStage.showAndWait();

            // Execute logout if user clicked "Yes, Log Out"
            if (LogoutModalController.isConfirmed()) {
                if (Session.getInstance() != null) {
                    Session.getInstance().clearSession();
                }
                App.switchScene(event, "/view/login.fxml");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to display logout modal window.");
        }
    }
}