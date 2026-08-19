package com.ka4piece.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class LogoutModalController {

    @FXML private Button btnCancel;
    @FXML private Button btnLogout;

    private static boolean confirmed = false;

    @FXML
    public void initialize() {
        confirmed = false; // Reset state when modal opens
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        confirmed = false;
        closeModal(event);
    }

    @FXML
    private void handleConfirmLogout(ActionEvent event) {
        confirmed = true;
        closeModal(event);
    }

    private void closeModal(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    // THIS IS THE MISSING METHOD
    public static boolean isConfirmed() {
        return confirmed;
    }
}