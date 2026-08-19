package com.ka4piece.controller;

import com.ka4piece.app.App;
import com.ka4piece.manager.AuthManager;
import com.ka4piece.manager.ComplianceManager;
import com.ka4piece.manager.JobMatchManager;
import com.ka4piece.model.Household;
import com.ka4piece.model.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class OfficialDashboardController {

    private AuthManager authManager;
    private ComplianceManager complianceManager;
    private JobMatchManager jobMatchManager;

    public OfficialDashboardController(AuthManager authManager, ComplianceManager complianceManager, JobMatchManager jobMatchManager) {
        this.authManager = authManager;
        this.complianceManager = complianceManager;
        this.jobMatchManager = jobMatchManager;
    }

    public OfficialDashboardController() {
    }

    // --- MAIN DASHBOARD CONTROLS ---
    @FXML private Hyperlink linkAddHousehold;
    @FXML private TextField txtChildrenCount;
    @FXML private Button btnDecrement;
    @FXML private Button btnIncrement;

    // --- TABLE CONTROLS ---
    @FXML private TableView<?> tblHistory;
    @FXML private TableColumn<?, ?> colMonth;
    @FXML private TableColumn<?, ?> colHealth;
    @FXML private TableColumn<?, ?> colEducation;
    @FXML private TableColumn<?, ?> colFds;
    @FXML private TableColumn<?, ?> colStatus;

    // --- MODAL CONTROLS (Fallback if modal references this controller) ---
    @FXML private TextField txtHeadName;
    @FXML private TextField txtAddress;
    @FXML private TextField txtBarangay;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnRegister;
    @FXML private Label lblSuccessMessage;

    // --- SEARCH CONTROLS ---
    @FXML private TextField txtSearch;
    @FXML private VBox boxSearchResults;
    @FXML private Label lblResultCount;

    private final int MIN_CHILDREN = 0;
    private final int MAX_CHILDREN = 3;

    @FXML
    public void initialize() {
        if (txtChildrenCount != null) {
            txtChildrenCount.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    txtChildrenCount.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });
        }

        if (txtSearch != null) {
            txtSearch.textProperty().addListener((observable, oldValue, newValue) -> performSearch(newValue));
            performSearch("");
        }
    }

    // --- SEARCH METHODS ---

    @FXML
    private void handleSearch(ActionEvent event) {
        if (txtSearch != null) {
            performSearch(txtSearch.getText());
        }
    }

    private void performSearch(String query) {
        if (authManager == null) return;
        List<Household> results = authManager.searchHouseholds(query);

        if (lblResultCount != null) {
            lblResultCount.setText("RESULTS (" + results.size() + ")");
        }

        if (boxSearchResults != null) {
            boxSearchResults.getChildren().clear();
            for (Household h : results) {
                VBox itemBox = new VBox();
                itemBox.getStyleClass().add("list-item");
                itemBox.setSpacing(2.0);

                Label nameLabel = new Label(h.getHeadName() != null ? h.getHeadName() : "Unnamed");
                nameLabel.getStyleClass().add("list-item-title");

                String subText = "ID: " + (h.getHouseholdId() != null ? h.getHouseholdId() : "N/A");
                if (h.getAddress() != null && !h.getAddress().isBlank()) {
                    subText += "  •  " + h.getAddress();
                }
                Label subLabel = new Label(subText);
                subLabel.getStyleClass().add("list-item-sub");

                itemBox.getChildren().addAll(nameLabel, subLabel);
                itemBox.setOnMouseClicked(e -> selectHousehold(h));
                boxSearchResults.getChildren().add(itemBox);
            }
        }
    }

    private void selectHousehold(Household h) {
        // Selection hook for displaying details & history
    }

    // --- NAVBAR & PROFILE EVENT HANDLERS ---

    @FXML
    private void handleOpenViewProfile(ActionEvent event) {
        switchSceneFromButton(event, "/view/official_profile.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = App.loadFXML("/view/logout_modal.fxml");

            // Fallback load if modal is directly in resources root
            if (root == null) {
                root = App.loadFXML("/logout_modal.fxml");
            }

            FXMLLoader loader = new FXMLLoader();
            // Obtain controller from loaded root context or load modal scene
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            modalStage.initOwner(currentStage);
            modalStage.setTitle("Confirm Logout");
            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false);

            LogoutModalController modalController = (LogoutModalController) root.getProperties().get("controller");

            modalStage.showAndWait();

            // Perform logout if user confirmed in modal
            if (LogoutModalController.isConfirmed()) {
                if (Session.getInstance() != null) {
                    Session.getInstance().clearSession();
                }
                switchSceneFromButton(event, "/view/login.fxml");
            }
        } catch (Exception e) {
            // Direct fallback if modal loading encounters pathing variations
            if (Session.getInstance() != null) {
                Session.getInstance().clearSession();
            }
            switchSceneFromButton(event, "/view/login.fxml");
        }
    }

    // --- TAB NAVIGATION HANDLERS ---

    @FXML
    private void goToCompliance(MouseEvent event) {
        switchSceneFromMouse(event, "/view/official_compliance.fxml");
    }

    @FXML
    private void goToJobVacancies(MouseEvent event) {
        switchSceneFromMouse(event, "/view/official_job_vacancies.fxml");
    }

    // --- MODAL DIALOG HANDLERS ---

    @FXML
    private void openAddHouseholdModal(ActionEvent event) {
        try {
            Parent root = App.loadFXML("/view/official_add_household.fxml");
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);

            if (linkAddHousehold != null && linkAddHousehold.getScene() != null) {
                modalStage.initOwner(linkAddHousehold.getScene().getWindow());
            }

            modalStage.setTitle("Register Household");
            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false);
            modalStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        if (lblSuccessMessage != null) {
            lblSuccessMessage.setVisible(true);
        }
    }

    // --- STEPPER HANDLERS ---

    @FXML
    private void handleIncrement() {
        if (txtChildrenCount == null) return;
        int currentCount = getCurrentCount();
        if (currentCount < MAX_CHILDREN) {
            txtChildrenCount.setText(String.valueOf(currentCount + 1));
        }
    }

    @FXML
    private void handleDecrement() {
        if (txtChildrenCount == null) return;
        int currentCount = getCurrentCount();
        if (currentCount > MIN_CHILDREN) {
            txtChildrenCount.setText(String.valueOf(currentCount - 1));
        }
    }

    // --- HELPER ROUTING METHODS ---

    private void switchSceneFromMouse(MouseEvent event, String fxmlPath) {
        App.switchScene(event, fxmlPath);
    }

    private void switchSceneFromButton(ActionEvent event, String fxmlPath) {
        App.switchScene(event, fxmlPath);
    }

    private URL resolveResource(String fxmlPath) {
        URL resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            resource = getClass().getResource("/view" + fxmlPath);
        }
        return resource;
    }

    private int getCurrentCount() {
        if (txtChildrenCount == null || txtChildrenCount.getText() == null) {
            return 0;
        }
        try {
            return Integer.parseInt(txtChildrenCount.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}