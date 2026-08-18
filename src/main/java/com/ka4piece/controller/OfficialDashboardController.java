package com.ka4piece.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class OfficialDashboardController {

    /* MAIN DASHBOARD CONTROLS */
    @FXML private Hyperlink linkAddHousehold;
    @FXML private TextField txtChildrenCount;
    @FXML private Button btnDecrement;
    @FXML private Button btnIncrement;

    /* COMPLIANCE TABLE CONTROLS */
    @FXML private TableView<ComplianceHistoryRecord> tblHistory;
    @FXML private TableColumn<ComplianceHistoryRecord, String> colMonth;
    @FXML private TableColumn<ComplianceHistoryRecord, String> colHealth;
    @FXML private TableColumn<ComplianceHistoryRecord, String> colEducation;
    @FXML private TableColumn<ComplianceHistoryRecord, String> colFds;
    @FXML private TableColumn<ComplianceHistoryRecord, String> colStatus;

    /* JOB VACANCIES FORM & DROPDOWNS */
    @FXML private TextField txtJobTitle;
    @FXML private ComboBox<String> cmbEducationRequirement;
    @FXML private TextField txtSkills;
    @FXML private TextField txtExperience;
    @FXML private TextField txtLocation;
    @FXML private TextField txtCompensation;
    @FXML private ComboBox<String> cmbEmploymentType;
    @FXML private Button btnEnterVacancy;

    /* APPLICANT RANKING STATUS DROPDOWNS */
    @FXML private ComboBox<String> cmbStatusRow1;
    @FXML private ComboBox<String> cmbStatusRow2;

    /* MODAL CONTROLS */
    @FXML private TextField txtHeadName;
    @FXML private TextField txtAddress;
    @FXML private TextField txtBarangay;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnRegister;
    @FXML private Label lblSuccessMessage;

    private final int MIN_CHILDREN = 0;
    private final int MAX_CHILDREN = 3;

    @FXML
    public void initialize() {
        // STEPPER CONTROL FOR HOUSEHOLD CHILDREN COUNT
        if (txtChildrenCount != null) {
            txtChildrenCount.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    txtChildrenCount.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });
        }

        // INITIALIZE COMPLIANCE TABLE IF PRESENT IN CURRENT SCENE
        if (tblHistory != null) {
            colMonth.setCellValueFactory(new PropertyValueFactory<>("month"));
            colHealth.setCellValueFactory(new PropertyValueFactory<>("health"));
            colEducation.setCellValueFactory(new PropertyValueFactory<>("education"));
            colFds.setCellValueFactory(new PropertyValueFactory<>("fds"));
            colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

            setupIconColumn(colHealth);
            setupIconColumn(colEducation);
            setupIconColumn(colFds);
            setupStatusColumn(colStatus);

            loadComplianceHistory();
        }

        // INITIALIZE JOB VACANCY DROPDOWNS IF PRESENT IN CURRENT SCENE
        initializeJobVacancyDropdowns();
    }

    private void initializeJobVacancyDropdowns() {
        // Populates Education Requirement ComboBox
        if (cmbEducationRequirement != null) {
            cmbEducationRequirement.setItems(FXCollections.observableArrayList(
                    "Elementary Graduate",
                    "High School Graduate",
                    "Vocational / Short Course",
                    "College Undergraduate",
                    "Bachelor's Degree",
                    "Master's / Doctoral Degree"
            ));
        }

        // Populates Employment Type ComboBox
        if (cmbEmploymentType != null) {
            cmbEmploymentType.setItems(FXCollections.observableArrayList(
                    "Full-time",
                    "Part-time"
            ));
        }

        // Populates Applicant Status ComboBoxes with options: N/A, For Interview, Applied
        ObservableList<String> statusOptions = FXCollections.observableArrayList("N/A", "For Interview", "Applied");

        if (cmbStatusRow1 != null) {
            cmbStatusRow1.setItems(statusOptions);
            cmbStatusRow1.setValue("For Interview"); // Set default initial state
        }

        if (cmbStatusRow2 != null) {
            cmbStatusRow2.setItems(statusOptions);
            cmbStatusRow2.setValue("Applied"); // Set default initial state
        }
    }

    private void setupIconColumn(TableColumn<ComplianceHistoryRecord, String> column) {
        column.setCellFactory(col -> new TableCell<ComplianceHistoryRecord, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label icon = new Label(item);
                    if ("✔".equals(item)) {
                        icon.getStyleClass().add("icon-check");
                    } else {
                        icon.getStyleClass().add("icon-cross");
                    }
                    setAlignment(Pos.CENTER);
                    setGraphic(icon);
                    setText(null);
                }
            }
        });
    }

    private void setupStatusColumn(TableColumn<ComplianceHistoryRecord, String> column) {
        column.setCellFactory(col -> new TableCell<ComplianceHistoryRecord, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if ("Compliant".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("status-badge-compliant");
                    } else {
                        badge.getStyleClass().add("status-badge-noncompliant");
                    }
                    setAlignment(Pos.CENTER);
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
    }

    private void loadComplianceHistory() {
        ObservableList<ComplianceHistoryRecord> historyList = FXCollections.observableArrayList(
                new ComplianceHistoryRecord("September", "✔", "✔", "✔", "Compliant"),
                new ComplianceHistoryRecord("August", "✔", "✖", "✔", "Non-Compliant"),
                new ComplianceHistoryRecord("July", "✔", "✔", "✔", "Compliant")
        );

        tblHistory.setItems(historyList);
    }

    /* NAVBAR & PROFILE HANDLERS */
    @FXML
    private void handleOpenViewProfile(ActionEvent event) {
        switchSceneFromButton(event, "/official_profile.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        switchSceneFromButton(event, "/login.fxml");
    }

    /* TAB NAVIGATION HANDLERS */
    @FXML
    private void goToCompliance(ActionEvent event) {
        switchSceneFromButton(event, "/official_compliance.fxml");
    }

    @FXML
    private void goToJobVacancies(ActionEvent event) {
        switchSceneFromButton(event, "/official_job_vacancies.fxml");
    }

    /* MODAL DIALOG HANDLERS */
    @FXML
    private void openAddHouseholdModal(ActionEvent event) {
        try {
            String fxmlPath = "/official_add_household.fxml";
            URL resource = resolveResource(fxmlPath);

            if (resource == null) {
                System.err.println("Could not locate modal resource file: " + fxmlPath);
                return;
            }

            Parent root = FXMLLoader.load(resource);
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

    /* STEPPER HANDLERS */
    @FXML
    private void handleIncrement(ActionEvent event) {
        if (txtChildrenCount == null) return;
        int currentCount = getCurrentCount();
        if (currentCount < MAX_CHILDREN) {
            txtChildrenCount.setText(String.valueOf(currentCount + 1));
        }
    }

    @FXML
    private void handleDecrement(ActionEvent event) {
        if (txtChildrenCount == null) return;
        int currentCount = getCurrentCount();
        if (currentCount > MIN_CHILDREN) {
            txtChildrenCount.setText(String.valueOf(currentCount - 1));
        }
    }

    /* HELPER ROUTING METHODS */
    private void switchSceneFromButton(ActionEvent event, String fxmlPath) {
        try {
            URL resource = resolveResource(fxmlPath);
            if (resource == null) {
                System.err.println("Could not locate resource file: " + fxmlPath);
                return;
            }

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