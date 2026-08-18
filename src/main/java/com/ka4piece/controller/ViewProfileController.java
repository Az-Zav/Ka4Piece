package com.ka4piece.controller;

import com.ka4piece.model.BarangayOfficial;
import com.ka4piece.model.Household;
import com.ka4piece.model.Session;
import com.ka4piece.repository.AuthRepository;
import com.ka4piece.repository.DbConfig;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;

public class ViewProfileController {

    // --- GENERAL CONTROLS ---
    @FXML private Button btnToggleEdit;
    @FXML private TextField txtUserId;
    @FXML private Label lblFullName;
    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
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
    @FXML private VBox boxPassword;
    @FXML private HBox boxEditActions;

    private boolean isEditMode = false;
    private Session activeSession;
    private AuthRepository authRepository;
    private DbConfig dbconfig;

    @FXML
    public void initialize() throws IOException {
        dbconfig = new DbConfig("db.properties");
        if (txtUserId != null) {
            txtUserId.setEditable(false);
        }

        if (cmbStatus != null) {
            cmbStatus.setItems(FXCollections.observableArrayList("Active", "Exited"));
            cmbStatus.valueProperty().addListener((obs, oldVal, newVal) -> toggleExitDetails("Exited".equalsIgnoreCase(newVal)));
        }

        authRepository = new AuthRepository(dbconfig.getJdbcurl(), dbconfig.getUsername(), dbconfig.getPassword());

        // Fetch active global session instance
        activeSession = Session.getInstance();

        autoPopulateProfileData();
        setEditMode(false);
    }

    private void autoPopulateProfileData() {
        if (activeSession == null || activeSession.getUserId() == null) {
            txtUserId.setText("N/A");
            txtName.setText("Guest User");
            if (txtEmail != null) txtEmail.setText("guest@ka4piece.com");
            return;
        }

        boolean isOfficial = "OFFICIAL".equalsIgnoreCase(activeSession.getRole());

        if (isOfficial) {
            lblFullName.setText("OFFICIAL NAME");

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
                txtUserId.setText(official.getOfficialId());
                txtName.setText(official.getName());
                if (txtEmail != null) txtEmail.setText(official.getEmail());
                if (chkIsAdmin != null) chkIsAdmin.setSelected(official.isAdmin());
            } else {
                txtUserId.setText(activeSession.getUserId());
                txtName.setText(activeSession.getDisplayName());
            }

        } else {
            lblFullName.setText("HEAD OF HOUSEHOLD NAME");

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
                txtUserId.setText(household.getHouseholdId());
                txtName.setText(household.getHeadName());
                if (txtEmail != null) txtEmail.setText(household.getEmail());
                if (txtAddress != null) txtAddress.setText(household.getAddress());
                if (txtBarangay != null) txtBarangay.setText(household.getBarangay());
                if (cmbStatus != null) cmbStatus.setValue(household.getStatus() != null ? household.getStatus() : "Active");
                if (txtExitReason != null) txtExitReason.setText(household.getExitReason());
                if (dpExitDate != null) dpExitDate.setValue(household.getExitDate());
            } else {
                txtUserId.setText(activeSession.getUserId());
                txtName.setText(activeSession.getDisplayName());
            }
        }
    }

    private void setEditMode(boolean enable) {
        isEditMode = enable;

        if (txtUserId != null) txtUserId.setEditable(false);

        txtName.setEditable(enable);
        if (txtEmail != null) txtEmail.setEditable(enable);

        if (txtAddress != null) txtAddress.setEditable(enable);
        if (txtBarangay != null) txtBarangay.setEditable(enable);

        if (cmbStatus != null) cmbStatus.setDisable(!enable);
        if (txtExitReason != null) txtExitReason.setEditable(enable);
        if (dpExitDate != null) dpExitDate.setDisable(!enable);
        if (chkIsAdmin != null) chkIsAdmin.setDisable(!enable);

        if (boxPassword != null) {
            boxPassword.setVisible(enable);
            boxPassword.setManaged(enable);
        }
        if (boxEditActions != null) {
            boxEditActions.setVisible(enable);
            boxEditActions.setManaged(enable);
        }

        if (btnToggleEdit != null) {
            btnToggleEdit.setVisible(!enable);
            btnToggleEdit.setManaged(!enable);
        }

        if (!enable && txtPassword != null) {
            txtPassword.clear();
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
    }

    @FXML
    private void handleCancelEdit(ActionEvent event) {
        autoPopulateProfileData();
        setEditMode(false);
        showMessage("Changes cancelled.", false);
    }

    @FXML
    private void handleSaveProfile(ActionEvent event) {
        String newName = txtName.getText().trim();
        String newEmail = txtEmail != null ? txtEmail.getText().trim() : "";
        String newPassword = txtPassword != null ? txtPassword.getText() : "";

        if (newName.isEmpty()) {
            showMessage("Name cannot be empty.", true);
            return;
        }

        if (activeSession != null && authRepository != null) {
            activeSession.setDisplayName(newName);
            String userId = activeSession.getUserId();

            boolean isOfficial = "OFFICIAL".equalsIgnoreCase(activeSession.getRole());

            if (isOfficial) {
                // Update Official Profile
                authRepository.updateOfficialProfile(userId, newName, newEmail);

                // Update Password if provided
                if (!newPassword.isEmpty()) {
                    authRepository.updateOfficialPassword(userId, newPassword);
                }
            } else {
                // Update Household Profile
                String address = txtAddress != null ? txtAddress.getText().trim() : "";
                String barangay = txtBarangay != null ? txtBarangay.getText().trim() : "";
                authRepository.updateHouseholdProfile(userId, newName, address, barangay, newEmail);

                // Update Exit/Status Details
                String status = cmbStatus != null && cmbStatus.getValue() != null ? cmbStatus.getValue() : "Active";
                String exitReason = txtExitReason != null ? txtExitReason.getText().trim() : "";
                LocalDate exitDate = dpExitDate != null ? dpExitDate.getValue() : null;
                authRepository.updateHouseholdExitStatus(userId, status, exitReason, exitDate);

                // Update Password if provided
                if (!newPassword.isEmpty()) {
                    authRepository.updateHouseholdPassword(userId, newPassword);
                }
            }
        }

        setEditMode(false);
        showMessage("Profile updated successfully!", false);
    }

    // --- NAVIGATION HANDLERS ---

    @FXML
    private void handleOpenViewProfile(ActionEvent event) {
        switchSceneFromButton(event, "/view_profile.fxml");
    }

    @FXML
    private void goToCompliance(MouseEvent event) {
        switchSceneFromMouse(event, "/compliance.fxml");
    }

    @FXML
    private void goToJobVacancies(MouseEvent event) {
        switchSceneFromMouse(event, "/job_vacancies.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        if (Session.getInstance() != null) {
            Session.getInstance().clearSession();
        }
        switchSceneFromButton(event, "/login.fxml");
    }

    // --- ROUTING HELPERS ---

    private void switchSceneFromButton(ActionEvent event, String fxmlPath) {
        try {
            URL resource = resolveResource(fxmlPath);
            if (resource == null) return;

            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchSceneFromMouse(MouseEvent event, String fxmlPath) {
        try {
            URL resource = resolveResource(fxmlPath);
            if (resource == null) return;

            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private URL resolveResource(String fxmlPath) {
        URL resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            resource = getClass().getResource("/view" + fxmlPath);
        }
        return resource;
    }

    private void showMessage(String msg, boolean isError) {
        if (lblMessage != null) {
            lblMessage.setText(msg);
            lblMessage.setTextFill(isError ? Color.RED : Color.GREEN);
            lblMessage.setVisible(true);
        }
    }
}