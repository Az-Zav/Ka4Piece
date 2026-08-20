package com.ka4piece.controller;

import com.ka4piece.manager.JobMatchManager;
import com.ka4piece.model.JobseekerProfile;
import com.ka4piece.model.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AddProfileController {

    @FXML private TextField memberNameField;
    @FXML private ComboBox<String> educationComboBox;
    @FXML private TextArea skillsArea;
    @FXML private TextField experienceField;
    @FXML private TextField barangayField;

    private final JobMatchManager jobMatchManager;

    public AddProfileController(JobMatchManager jobMatchManager) {
        this.jobMatchManager = jobMatchManager;
    }

    @FXML
    private void handleSaveProfile(ActionEvent event) {
        String memberName = memberNameField.getText();
        String education = educationComboBox.getValue();
        String skills = skillsArea.getText();
        String experience = experienceField.getText();
        String barangay = barangayField.getText();

        String householdId = Session.getInstance().getUserId();
        if (householdId == null) {
            householdId = "HOUSEHOLD-GUEST";
        }

        int expYears = 0;
        try {
            if (experience != null && !experience.trim().isEmpty()) {
                expYears = Integer.parseInt(experience.trim());
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid experience year value: " + experience);
        }

        List<String> skillsList = Arrays.stream((skills != null ? skills : "").split("[,;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        JobseekerProfile profile = new JobseekerProfile(
                null,
                householdId,
                memberName != null ? memberName.trim() : "",
                education != null ? education : "",
                barangay != null ? barangay.trim() : "",
                skillsList,
                expYears,
                new java.util.HashMap<>()
        );

        if (jobMatchManager != null) {
            jobMatchManager.createProfile(profile);
        }

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