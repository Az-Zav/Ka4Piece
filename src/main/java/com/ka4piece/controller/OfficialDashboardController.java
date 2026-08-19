package com.ka4piece.controller;

import com.ka4piece.app.App;
import com.ka4piece.manager.AuthManager;
import com.ka4piece.manager.ComplianceManager;
import com.ka4piece.manager.JobMatchManager;
import com.ka4piece.model.Household;
import com.ka4piece.model.Session;
import com.ka4piece.model.ComplianceRecord;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
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

    // Track the currently selected household
    private Household selectedHousehold;

    // --- NAME CARD UI CONTROLS (Right Panel Header) ---
    @FXML private Label lblHeaderName;
    @FXML private Label lblHeaderSubDetails; // For Barangay & ID

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

    // --- HOUSEHOLD PROFILE BADGES ---
    @FXML private Label lblStatusBadge;
    @FXML private Label lblPregnantBadge;
    @FXML private Label lblHas0_5Badge;
    @FXML private Label lblElemBadge;
    @FXML private Label lblJhsBadge;
    @FXML private Label lblShsBadge;

    // --- COMPLIANCE FORM CONTROLS ---
    @FXML private CheckBox chkPregnancyCare;
    @FXML private CheckBox chkHealthCheckup;
    @FXML private CheckBox chkDeworming;
    @FXML private CheckBox chkDaycare;
    @FXML private CheckBox chkSchoolAttendance;
    @FXML private CheckBox chkFDS;
    @FXML private Label lblEligibleChildrenCount; // e.g., "out of 3 eligible"
    @FXML private Spinner<Integer> elementaryCountSpinner;
    @FXML private Spinner<Integer> juniorHighCountSpinner;
    @FXML private Spinner<Integer> seniorHighCountSpinner;

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

    private void selectHousehold(com.ka4piece.model.Household h) {
        if (h == null) return;
        
        // Save current selection context
        this.selectedHousehold = h;

        // 1. EXTRACT & FORMAT HEADER TEXT VARIABLES
        String headName = (h.getHeadName() != null && !h.getHeadName().isBlank()) 
                ? h.getHeadName() 
                : "Unnamed Household";

        String householdId = (h.getHouseholdId() != null) 
                ? h.getHouseholdId() 
                : "N/A";

        String addressInfo = (h.getBarangay() != null && !h.getBarangay().isBlank()) 
                ? h.getBarangay() 
                : ((h.getAddress() != null && !h.getAddress().isBlank()) ? h.getAddress() : "No Address Listed");

        String subDetailsText = addressInfo + "  •  ID: " + householdId;

        // 2. UPDATE HEADER UI LABELS
        if (lblHeaderName != null) {
            lblHeaderName.setText(headName);
        }

        if (lblHeaderSubDetails != null) {
            lblHeaderSubDetails.setText(subDetailsText);
        }

        // 3. FETCH VALUES FROM MODEL
        boolean hasPregnant = h.isHasPregnantMember();
        boolean has0to5 = h.isHas0to5Member();
        int elemCount = h.getElemCount();
        int jhsCount = h.getJhsCount();
        int shsCount = h.getShsCount();
        boolean hasAnySchoolChild = (elemCount > 0) || (jhsCount > 0) || (shsCount > 0);

        // 4. UPDATE HEADER BADGES (VISIBLE / MANAGED TOGGLES & TEXT)
        
        // Pregnant Badge
        if (lblPregnantBadge != null) {
            lblPregnantBadge.setVisible(hasPregnant);
            lblPregnantBadge.setManaged(hasPregnant);
        }

        // 0-5 Children Badge
        if (lblHas0_5Badge != null) {
            lblHas0_5Badge.setVisible(has0to5);
            lblHas0_5Badge.setManaged(has0to5);
        }

        // Elementary Badge
        if (lblElemBadge != null) {
            boolean hasElem = elemCount > 0;
            lblElemBadge.setText(elemCount + " ELEM");
            lblElemBadge.setVisible(hasElem);
            lblElemBadge.setManaged(hasElem);
        }

        // Junior High Badge
        if (lblJhsBadge != null) {
            boolean hasJhs = jhsCount > 0;
            lblJhsBadge.setText(jhsCount + " JHS");
            lblJhsBadge.setVisible(hasJhs);
            lblJhsBadge.setManaged(hasJhs);
        }

        // Senior High Badge
        if (lblShsBadge != null) {
            boolean hasShs = shsCount > 0;
            lblShsBadge.setText(shsCount + " SHS");
            lblShsBadge.setVisible(hasShs);
            lblShsBadge.setManaged(hasShs);
        }

        // Populate compliance form
        populateComplianceForm(h);
    }

    private void populateComplianceForm(com.ka4piece.model.Household household) {
        if (household == null || complianceManager == null) return;

        // 8. State-tracking fix — pre-fetch current month's record before rendering
        com.ka4piece.model.ComplianceRecord currentRecord = complianceManager.getCurrentStatus(household.getHouseholdId());

        if (currentRecord != null) {
            if (chkPregnancyCare != null) chkPregnancyCare.setSelected(currentRecord.isPregnancyCareStatus());
            if (chkHealthCheckup != null) chkHealthCheckup.setSelected(currentRecord.isChild0to5HealthStatus());
            if (chkDeworming != null) chkDeworming.setSelected(currentRecord.isDewormingStatus());
            if (chkDaycare != null) chkDaycare.setSelected(currentRecord.isDaycareAttendanceStatus());
            if (chkSchoolAttendance != null) chkSchoolAttendance.setSelected(currentRecord.isSchoolAttendanceStatus());
            if (chkFDS != null) chkFDS.setSelected(currentRecord.isFdsAttendanceStatus());

            if (elementaryCountSpinner != null) {
                int val = Math.min(currentRecord.getElementaryCount(), household.getElemCount());
                elementaryCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        0, household.getElemCount(), val));
            }
            if (juniorHighCountSpinner != null) {
                int val = Math.min(currentRecord.getJuniorHighCount(), household.getJhsCount());
                juniorHighCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        0, household.getJhsCount(), val));
            }
            if (seniorHighCountSpinner != null) {
                int val = Math.min(currentRecord.getSeniorHighCount(), household.getShsCount());
                seniorHighCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        0, household.getShsCount(), val));
            }

            if (txtChildrenCount != null) {
                int total = currentRecord.getElementaryCount() + currentRecord.getJuniorHighCount() + currentRecord.getSeniorHighCount();
                txtChildrenCount.setText(String.valueOf(total));
            }
        } else {
            // Reset to defaults if currentRecord = null
            if (chkPregnancyCare != null) chkPregnancyCare.setSelected(false);
            if (chkHealthCheckup != null) chkHealthCheckup.setSelected(false);
            if (chkDeworming != null) chkDeworming.setSelected(false);
            if (chkDaycare != null) chkDaycare.setSelected(false);
            if (chkSchoolAttendance != null) chkSchoolAttendance.setSelected(false);
            if (chkFDS != null) chkFDS.setSelected(false);

            if (elementaryCountSpinner != null) {
                elementaryCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        0, household.getElemCount(), 0));
            }
            if (juniorHighCountSpinner != null) {
                juniorHighCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        0, household.getJhsCount(), 0));
            }
            if (seniorHighCountSpinner != null) {
                seniorHighCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        0, household.getShsCount(), 0));
            }

            if (txtChildrenCount != null) {
                txtChildrenCount.setText("0");
            }
        }

        if (lblEligibleChildrenCount != null) {
            int eligibleTotal = household.getElemCount() + household.getJhsCount() + household.getShsCount();
            lblEligibleChildrenCount.setText("out of " + eligibleTotal + " eligible");
        }

        // 9. Compliance form — conditionally suppress non-applicable fields
        boolean hasChildren = (household.getElemCount() > 0 || household.getJhsCount() > 0 || household.getShsCount() > 0);

        if (chkPregnancyCare != null) {
            chkPregnancyCare.setDisable(!household.isHasPregnantMember());
            chkPregnancyCare.setManaged(household.isHasPregnantMember());
        }
        if (chkHealthCheckup != null) {
            chkHealthCheckup.setDisable(!household.isHas0to5Member());
            chkHealthCheckup.setManaged(household.isHas0to5Member());
        }
        if (chkDeworming != null) {
            chkDeworming.setDisable(!hasChildren);
            chkDeworming.setManaged(hasChildren);
        }
    }

    @FXML
    private void handleRecordMonthlyStatus(ActionEvent event) {
        if (selectedHousehold == null || complianceManager == null) {
            return;
        }

        String householdId = selectedHousehold.getHouseholdId();
        String currentMonth = java.time.YearMonth.now().toString();

        // Check if row already exists
        com.ka4piece.model.ComplianceRecord existingRecord = complianceManager.getCurrentStatus(householdId);
        String recordType = "INITIAL";
        String correctionReason = null;
        if (existingRecord != null) {
            recordType = "CORRECTION";
            correctionReason = "Official updating compliance record";
        }

        boolean pregnancyCare = chkPregnancyCare != null && chkPregnancyCare.isSelected();
        boolean child0to5 = chkHealthCheckup != null && chkHealthCheckup.isSelected();
        boolean deworming = chkDeworming != null && chkDeworming.isSelected();
        boolean daycare = chkDaycare != null && chkDaycare.isSelected();
        boolean school = chkSchoolAttendance != null && chkSchoolAttendance.isSelected();
        boolean fds = chkFDS != null && chkFDS.isSelected();

        int elem = 0;
        if (elementaryCountSpinner != null) {
            elem = elementaryCountSpinner.getValue();
        } else if (txtChildrenCount != null) {
            // fallback if spinners are not present
            elem = getCurrentCount();
        }

        int jhs = 0;
        if (juniorHighCountSpinner != null) {
            jhs = juniorHighCountSpinner.getValue();
        }

        int shs = 0;
        if (seniorHighCountSpinner != null) {
            shs = seniorHighCountSpinner.getValue();
        }

        String officialId = Session.getInstance() != null ? Session.getInstance().getUserId() : null;

        com.ka4piece.model.ComplianceRecord record = new com.ka4piece.model.ComplianceRecord(
                householdId,
                currentMonth,
                pregnancyCare,
                child0to5,
                deworming,
                daycare,
                school,
                fds,
                elem,
                jhs,
                shs,
                officialId
        );

        try {
            complianceManager.recordConditionStatus(record);
            complianceManager.computeMonthlyGrant(record, recordType, correctionReason);
            
            // Refresh view
            selectHousehold(selectedHousehold);
        } catch (Exception e) {
            System.err.println("[OfficialDashboardController] Error recording compliance: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- NAVBAR & PROFILE EVENT HANDLERS ---

    @FXML
    private void handleOpenViewProfile(ActionEvent event) {
        NavigationUtils.switchSceneFromButton(event, "/view/official_profile.fxml");
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
                NavigationUtils.switchSceneFromButton(event, "/view/login.fxml");
            }
        } catch (Exception e) {
            // Direct fallback if modal loading encounters pathing variations
            if (Session.getInstance() != null) {
                Session.getInstance().clearSession();
            }
            NavigationUtils.switchSceneFromButton(event, "/view/login.fxml");
        }
    }

    // --- TAB NAVIGATION HANDLERS ---

    @FXML
    private void goToCompliance(ActionEvent event) {
        NavigationUtils.switchSceneFromButton(event, "/view/official_compliance.fxml");
    }

    @FXML
    private void goToJobVacancies(ActionEvent event) {
        NavigationUtils.switchSceneFromButton(event, "/view/official_job_vacancies.fxml");
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
            
            //wait for modal to open and pause execution until modal is closed
            modalStage.showAndWait();

            /// REFRESH TRIGGER: Automatically refresh search results once the modal closes
            if (txtSearch != null) {
                performSearch(txtSearch.getText());
            } else {
                performSearch(""); // Fallback to clear list refresh
            }


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
        if (selectedHousehold != null) {
            int maxTotal = selectedHousehold.getElemCount() + selectedHousehold.getJhsCount() + selectedHousehold.getShsCount();
            if (currentCount < maxTotal) {
                txtChildrenCount.setText(String.valueOf(currentCount + 1));
            }
        } else {
            if (currentCount < MAX_CHILDREN) {
                txtChildrenCount.setText(String.valueOf(currentCount + 1));
            }
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