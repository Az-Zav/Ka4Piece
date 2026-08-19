package com.ka4piece.controller;

import com.ka4piece.app.App;
import com.ka4piece.model.BarangayOfficial;
import com.ka4piece.model.Household;
import com.ka4piece.model.Session;
import com.ka4piece.repository.AuthRepository;
import com.ka4piece.repository.DbConfig;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;

public class OfficialProfileController {

    // --- GENERAL CONTROLS ---
    @FXML private Button btnToggleEdit;
    @FXML private Button btnChangePassword;
    @FXML private TextField txtUserId;
    @FXML private Label lblFullName;
    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private Label lblMessage;

    // --- HOUSEHOLD FIELDS ---
    @FXML private VBox boxHouseholdFields;
    @FXML private TextField txtAddress;
    @FXML private TextField txtBarangay;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private VBox boxExitDetails;
    @FXML private TextField txtExitReason;
    @FXML private DatePicker dpExitDate;

    // --- OFFICIAL FIELDS ---
    @FXML private VBox boxOfficialFields;
    @FXML private CheckBox chkIsAdmin;

    // --- CONTAINER PANELS ---
    @FXML private HBox boxEditActions;

    private boolean isEditMode = false;
    private Session activeSession;
    private AuthRepository authRepository;

    public OfficialProfileController(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public OfficialProfileController() {
    }

    @FXML
    public void initialize() {
        if (txtUserId != null) {
            txtUserId.setEditable(false);
        }

        if (cmbStatus != null) {
            cmbStatus.setItems(FXCollections.observableArrayList("Active", "Exited"));
            cmbStatus.valueProperty().addListener((obs, oldVal, newVal) -> toggleExitDetails("Exited".equalsIgnoreCase(newVal)));
        }

        // Load DB config as fallback to prevent null repository crashes
        if (authRepository == null) {
            try {
                DbConfig config = new DbConfig("db.properties");
                authRepository = new AuthRepository(config.getJdbcurl(), config.getUsername(), config.getPassword());
            } catch (Exception e) {
                System.err.println("Could not load db.properties: " + e.getMessage());
            }
        }

        activeSession = Session.getInstance();

        autoPopulateProfileData();
        setEditMode(false);
    }

    private void autoPopulateProfileData() {
        if (activeSession == null || activeSession.getUserId() == null) {
            if (txtUserId != null) txtUserId.setText("N/A");
            if (txtName != null) txtName.setText("Guest User");
            if (txtEmail != null) txtEmail.setText("guest@ka4piece.com");
            return;
        }

        boolean isOfficial = "OFFICIAL".equalsIgnoreCase(activeSession.getRole());

        if (isOfficial) {
            if (lblFullName != null) lblFullName.setText("OFFICIAL NAME");

            if (boxHouseholdFields != null) {
                boxHouseholdFields.setVisible(false);
                boxHouseholdFields.setManaged(false);
            }
            if (boxOfficialFields != null) {
                boxOfficialFields.setVisible(true);
                boxOfficialFields.setManaged(true);
            }

            BarangayOfficial official = authRepository != null
                    ? authRepository.findOfficialById(activeSession.getUserId())
                    : null;

            if (official != null) {
                if (txtUserId != null) txtUserId.setText(official.getOfficialId());
                if (txtName != null) txtName.setText(official.getName());
                if (txtEmail != null) txtEmail.setText(official.getEmail());
                if (chkIsAdmin != null) chkIsAdmin.setSelected(official.isAdmin());
            } else {
                if (txtUserId != null) txtUserId.setText(activeSession.getUserId());
                if (txtName != null) txtName.setText(activeSession.getDisplayName());
            }

        } else {
            if (lblFullName != null) lblFullName.setText("HEAD OF HOUSEHOLD NAME");

            if (boxOfficialFields != null) {
                boxOfficialFields.setVisible(false);
                boxOfficialFields.setManaged(false);
            }
            if (boxHouseholdFields != null) {
                boxHouseholdFields.setVisible(true);
                boxHouseholdFields.setManaged(true);
            }

            Household household = authRepository != null
                    ? authRepository.findHouseholdById(activeSession.getUserId())
                    : null;

            if (household != null) {
                if (txtUserId != null) txtUserId.setText(household.getHouseholdId());
                if (txtName != null) txtName.setText(household.getHeadName());
                if (txtEmail != null) txtEmail.setText(household.getEmail());
                if (txtAddress != null) txtAddress.setText(household.getAddress());
                if (txtBarangay != null) txtBarangay.setText(household.getBarangay());
                if (cmbStatus != null) cmbStatus.setValue(household.getStatus() != null ? household.getStatus() : "Active");
                if (txtExitReason != null) txtExitReason.setText(household.getExitReason());
                if (dpExitDate != null) dpExitDate.setValue(household.getExitDate());
            } else {
                if (txtUserId != null) txtUserId.setText(activeSession.getUserId());
                if (txtName != null) txtName.setText(activeSession.getDisplayName());
            }
        }
    }

    private void setEditMode(boolean enable) {
        isEditMode = enable;

        if (txtUserId != null) txtUserId.setEditable(false);

        if (txtName != null) txtName.setEditable(enable);
        if (txtEmail != null) txtEmail.setEditable(enable);

        if (txtAddress != null) txtAddress.setEditable(enable);
        if (txtBarangay != null) txtBarangay.setEditable(enable);

        if (cmbStatus != null) cmbStatus.setDisable(!enable);
        if (txtExitReason != null) txtExitReason.setEditable(enable);
        if (dpExitDate != null) dpExitDate.setDisable(!enable);
        if (chkIsAdmin != null) chkIsAdmin.setDisable(!enable);

        if (boxEditActions != null) {
            boxEditActions.setVisible(enable);
            boxEditActions.setManaged(enable);
        }

        if (btnToggleEdit != null) {
            btnToggleEdit.setVisible(!enable);
            btnToggleEdit.setManaged(!enable);
        }

        if (btnChangePassword != null) {
            btnChangePassword.setVisible(!enable);
            btnChangePassword.setManaged(!enable);
        }
    }

    private void toggleExitDetails(boolean show) {
        if (boxExitDetails != null) {
            boxExitDetails.setVisible(show);
            boxExitDetails.setManaged(show);
        }
    }

    @FXML
    private void handleToggleEditMode(ActionEvent event) {
        setEditMode(true);
        showMessage("", false);
    }

    @FXML
    private void handleCancelEdit(ActionEvent event) {
        autoPopulateProfileData();
        setEditMode(false);
        showMessage("Changes cancelled.", false);
    }

    @FXML
    private void handleSaveProfile(ActionEvent event) {
        String newName = txtName != null ? txtName.getText().trim() : "";
        String newEmail = txtEmail != null ? txtEmail.getText().trim() : "";

        if (newName.isEmpty()) {
            showMessage("Name cannot be empty.", true);
            return;
        }

        if (activeSession != null && authRepository != null) {
            activeSession.setDisplayName(newName);
            String userId = activeSession.getUserId();

            boolean isOfficial = "OFFICIAL".equalsIgnoreCase(activeSession.getRole());

            if (isOfficial) {
                authRepository.updateOfficialProfile(userId, newName, newEmail);
            } else {
                String address = txtAddress != null ? txtAddress.getText().trim() : "";
                String barangay = txtBarangay != null ? txtBarangay.getText().trim() : "";
                authRepository.updateHouseholdProfile(userId, newName, address, barangay, newEmail);

                String status = cmbStatus != null && cmbStatus.getValue() != null ? cmbStatus.getValue() : "Active";
                String exitReason = txtExitReason != null ? txtExitReason.getText().trim() : "";
                LocalDate exitDate = dpExitDate != null ? dpExitDate.getValue() : null;
                authRepository.updateHouseholdExitStatus(userId, status, exitReason, exitDate);
            }
        }

        setEditMode(false);
        showMessage("Profile updated successfully!", false);
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        openModal(event, "/view/change_password_dialog.fxml", "Change Password");
    }

    // --- NAVIGATION HANDLERS ---

    @FXML
    private void handleOpenViewProfile(Event event) {
        App.switchScene(event, "/view/official_profile.fxml");
    }

    @FXML
    private void goToCompliance(Event event) {
        App.switchScene(event, "/view/official_compliance.fxml");
    }

    @FXML
    private void goToJobVacancies(Event event) {
        App.switchScene(event, "/view/official_job_vacancies.fxml");
    }

    @FXML
    private void handleLogout(Event event) {
        NavigationUtils.showLogoutModal(event);
    }

    // --- HELPER METHODS ---

    private void openModal(Event event, String path, String title) {
        try {
            String resourcePath = path.startsWith("/") ? path : "/" + path;
            URL location = App.class.getResource(resourcePath);

            if (location == null) {
                location = getClass().getResource(resourcePath);
            }
            if (location == null && resourcePath.startsWith("/view/")) {
                location = App.class.getResource(resourcePath.replace("/view/", "/"));
            }

            if (location == null) {
                showMessage("FXML file not found: " + path, true);
                return;
            }

            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error opening modal window.", true);
        }
    }

    private void showMessage(String msg, boolean isError) {
        if (lblMessage != null) {
            if (msg == null || msg.isEmpty()) {
                lblMessage.setVisible(false);
                lblMessage.setManaged(false);
            } else {
                lblMessage.setText(msg);
                lblMessage.setTextFill(isError ? Color.RED : Color.GREEN);
                lblMessage.setVisible(true);
                lblMessage.setManaged(true);
            }
        }
    }
}