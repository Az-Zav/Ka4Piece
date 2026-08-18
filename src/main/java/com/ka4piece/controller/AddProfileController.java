package com.ka4piece.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddProfileController {

    @FXML private TextField memberNameField;
    @FXML private ComboBox<String> educationComboBox;
    @FXML private TextArea skillsArea;
    @FXML private TextField experienceField;
    @FXML private TextField barangayField;

    @FXML
    private void handleSaveProfile(ActionEvent event) {
        String memberName = memberNameField.getText();
        String education = educationComboBox.getValue();
        String skills = skillsArea.getText();
        String experience = experienceField.getText();
        String barangay = barangayField.getText();

        // TODO: Pass data to your Database/Repository
        System.out.println("Saving Profile: " + memberName + " - " + education);

        closeStage(event);
    }

    @FXML
    private void handleClose(ActionEvent event) {
        closeStage(event);
    }

    private void closeStage(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}