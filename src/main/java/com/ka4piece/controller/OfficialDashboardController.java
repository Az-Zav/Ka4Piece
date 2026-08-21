package com.ka4piece.controller;

import com.ka4piece.app.App;
import com.ka4piece.manager.AuthManager;
import com.ka4piece.manager.ComplianceManager;
import com.ka4piece.manager.JobMatchManager;
import com.ka4piece.model.Household;
import com.ka4piece.model.Session;
import com.ka4piece.model.GrantBreakdown;

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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.text.Font;
import com.ka4piece.model.Vacancy;
import com.ka4piece.model.RankedCandidate;
import java.util.Arrays;
import java.util.stream.Collectors;

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

    // Track the currently selected vacancy
    private Vacancy selectedVacancy;

    // --- JOB VACANCIES UI CONTROLS ---
    @FXML private TextField txtJobTitle;
    @FXML private ComboBox<String> cmbEducationRequirement;
    @FXML private TextField txtSkills;
    @FXML private TextArea txtJobDescription;
    @FXML private TextField txtExperience;
    @FXML private TextField txtLocation;
    @FXML private TextField txtCompensation;
    @FXML private ComboBox<String> cmbEmploymentType;
    @FXML private Button btnEnterVacancy;

    @FXML private VBox boxPostedVacancies;

    @FXML private Label lblSelectedVacancyTitle;
    @FXML private Label lblSelectedVacancyDescription;
    @FXML private Label lblSelectedVacancyLocTypeSalary;
    @FXML private Label lblSelectedVacancyEducationExperience;
    @FXML private Label lblSelectedVacancySkills;
    @FXML private VBox boxRankedApplicants;

    @FXML private Label lblVacancyFormFeedback;
    @FXML private Button btnCancelEdit;
    @FXML private Button btnEditVacancy;

    // Track whether the form is in EDIT mode (vacancyId set) or CREATE mode (null)
    private String vacancyEditModeId = null;
    @FXML private Label lblHeaderName;
    @FXML private Label lblHeaderSubDetails; // For Barangay & ID

    // --- MAIN DASHBOARD CONTROLS ---
    @FXML private Hyperlink linkAddHousehold;
    @FXML private TextField txtChildrenCount;
    @FXML private Button btnDecrement;
    @FXML private Button btnIncrement;

    // --- DYNAMIC GRANT BREAKDOWN CONTROLS ---
    @FXML private Label lblHealthGrant;
    @FXML private Label lblEducationGrant;
    @FXML private Label lblRiceSubsidy;
    @FXML private Label lblTotalGrant;
    @FXML private Label lblGrantStatus;

    // --- TABLE CONTROLS ---
    @FXML private TableView<ComplianceHistoryRecord> tblHistory;
    @FXML private TableColumn<ComplianceHistoryRecord, String> colMonth;
    @FXML private TableColumn<ComplianceHistoryRecord, String> colHealth;
    @FXML private TableColumn<ComplianceHistoryRecord, String> colEducation;
    @FXML private TableColumn<ComplianceHistoryRecord, String> colFds;
    @FXML private TableColumn<ComplianceHistoryRecord, String> colStatus;

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
    @FXML private Label lblComplianceFeedback;
    @FXML private Button btnRecordCompliance;

    private final int MIN_CHILDREN = 0;

    @FXML
    public void initialize() {
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((observable, oldValue, newValue) -> performSearch(newValue));
            performSearch("");
        }

        // Validate txtChildrenCount to digits only
        if (txtChildrenCount != null) {
            txtChildrenCount.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    txtChildrenCount.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });
        }

        // Configure history table cell value factories
        if (colMonth != null) colMonth.setCellValueFactory(new PropertyValueFactory<>("month"));
        if (colHealth != null) colHealth.setCellValueFactory(new PropertyValueFactory<>("health"));
        if (colEducation != null) colEducation.setCellValueFactory(new PropertyValueFactory<>("education"));
        if (colFds != null) colFds.setCellValueFactory(new PropertyValueFactory<>("fds"));
        if (colStatus != null) colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Setup custom cell factories for table styling
        javafx.util.Callback<TableColumn<ComplianceHistoryRecord, String>, TableCell<ComplianceHistoryRecord, String>> iconCellFactory =
            column -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        getStyleClass().removeAll("icon-check", "icon-cross");
                        if (item.equalsIgnoreCase("Compliant") || item.equals("✓")) {
                            setText("✓");
                            getStyleClass().add("icon-check");
                        } else {
                            setText("✗");
                            getStyleClass().add("icon-cross");
                        }
                    }
                }
            };

        javafx.util.Callback<TableColumn<ComplianceHistoryRecord, String>, TableCell<ComplianceHistoryRecord, String>> statusCellFactory =
            column -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        getStyleClass().removeAll("status-badge-compliant", "status-badge-noncompliant");
                        if (item.equalsIgnoreCase("Compliant")) {
                            getStyleClass().add("status-badge-compliant");
                        } else {
                            getStyleClass().add("status-badge-noncompliant");
                        }
                    }
                }
            };

        if (colHealth != null) colHealth.setCellFactory(iconCellFactory);
        if (colEducation != null) colEducation.setCellFactory(iconCellFactory);
        if (colFds != null) colFds.setCellFactory(iconCellFactory);
        if (colStatus != null) colStatus.setCellFactory(statusCellFactory);

        // Wire checkbox listeners for real-time grant computation
        if (chkPregnancyCare != null) chkPregnancyCare.selectedProperty().addListener((obs, oldVal, newVal) -> handleFormChange());
        if (chkHealthCheckup != null) chkHealthCheckup.selectedProperty().addListener((obs, oldVal, newVal) -> handleFormChange());
        if (chkDeworming != null) chkDeworming.selectedProperty().addListener((obs, oldVal, newVal) -> handleFormChange());
        if (chkDaycare != null) chkDaycare.selectedProperty().addListener((obs, oldVal, newVal) -> handleFormChange());
        if (chkSchoolAttendance != null) chkSchoolAttendance.selectedProperty().addListener((obs, oldVal, newVal) -> handleFormChange());
        if (chkFDS != null) chkFDS.selectedProperty().addListener((obs, oldVal, newVal) -> handleFormChange());

        // Wire children count text listener for real-time grant computation
        if (txtChildrenCount != null) {
            txtChildrenCount.textProperty().addListener((obs, oldVal, newVal) -> handleFormChange());
        }

        // Initialize Job Vacancies screen if loaded
        if (txtJobTitle != null) {
            if (cmbEducationRequirement != null) {
                cmbEducationRequirement.getItems().setAll("HighSchool", "Vocational", "College");
            }
            if (cmbEmploymentType != null) {
                cmbEmploymentType.getItems().setAll("Full-time", "Part-time", "Contractual", "Temporary");
            }
            if (btnEnterVacancy != null) {
                btnEnterVacancy.setOnAction(e -> handleEnterVacancy());
            }
            if (btnCancelEdit != null) {
                btnCancelEdit.setOnAction(e -> cancelEditMode());
            }
            populatePostedVacancies();
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

        // Status Badge (ACTIVE / EXITED)
        if (lblStatusBadge != null) {
            boolean isExited = "EXITED".equalsIgnoreCase(h.getStatus());
            lblStatusBadge.setText(isExited ? "• EXITED" : "• ACTIVE");
            lblStatusBadge.getStyleClass().setAll(isExited ? "badge-gray" : "badge-active");
        }

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

        // Populate compliance history
        populateComplianceHistory(h);
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

            // Restore total count as the sum of all tiers from the existing record
            if (txtChildrenCount != null) {
                int total = currentRecord.getElementaryCount() + currentRecord.getJuniorHighCount() + currentRecord.getSeniorHighCount();
                txtChildrenCount.setText(String.valueOf(total));
            }

            // Existing record: update button text and show gray timestamp
            if (btnRecordCompliance != null) {
                btnRecordCompliance.setText("Update Monthly Status");
            }
            if (lblComplianceFeedback != null) {
                if (currentRecord.getRecordedAt() != null) {
                    lblComplianceFeedback.setText("Compliance Record last updated on: " + formatRecordedAtPHT(currentRecord.getRecordedAt()));
                    lblComplianceFeedback.setTextFill(javafx.scene.paint.Color.web("#64748B"));
                } else {
                    lblComplianceFeedback.setText("");
                }
            }
        } else {
            // Reset to defaults if currentRecord = null
            if (chkPregnancyCare != null) chkPregnancyCare.setSelected(false);
            if (chkHealthCheckup != null) chkHealthCheckup.setSelected(false);
            if (chkDeworming != null) chkDeworming.setSelected(false);
            if (chkDaycare != null) chkDaycare.setSelected(false);
            if (chkSchoolAttendance != null) chkSchoolAttendance.setSelected(false);
            if (chkFDS != null) chkFDS.setSelected(false);

            if (txtChildrenCount != null) {
                txtChildrenCount.setText("0");
            }

            // No existing record: show "Record" button text and hide feedback
            if (btnRecordCompliance != null) {
                btnRecordCompliance.setText("Record Monthly Status");
            }
            if (lblComplianceFeedback != null) {
                lblComplianceFeedback.setText("");
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
        }
        if (chkHealthCheckup != null) {
            chkHealthCheckup.setDisable(!household.isHas0to5Member());
        }
        if (chkDeworming != null) {
            chkDeworming.setDisable(!hasChildren && !household.isHas0to5Member());
        }

        if (chkDaycare != null) {
            chkDaycare.setDisable(!household.isHas0to5Member());
        }

        if (chkSchoolAttendance != null) {
            chkSchoolAttendance.setDisable(!hasChildren);
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

        // Distribute total count using SHS-first tier precedence
        int[] tiers = distributeTiersBySHSPrecedence(getCurrentCount());
        int elem = tiers[0];
        int jhs = tiers[1];
        int shs = tiers[2];

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

        boolean isUpdate = existingRecord != null;

        try {
            complianceManager.recordConditionStatus(record);
            complianceManager.computeMonthlyGrant(record, recordType, correctionReason);

            // Refresh compliance history table immediately
            populateComplianceHistory(selectedHousehold);

            // 1. Immediately disable button and show green success feedback
            if (btnRecordCompliance != null) {
                btnRecordCompliance.setDisable(true);
            }
            if (lblComplianceFeedback != null) {
                String successMsg = isUpdate
                        ? "Compliance Record successfully updated."
                        : "Compliance record successfully recorded.";
                lblComplianceFeedback.setText(successMsg);
                lblComplianceFeedback.setTextFill(javafx.scene.paint.Color.web("#16A34A"));
            }

            // 2. After 5 seconds, re-enable and transition to update state
            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(5));
            delay.setOnFinished(e -> {
                if (btnRecordCompliance != null) {
                    btnRecordCompliance.setDisable(false);
                    btnRecordCompliance.setText("Update Monthly Status");
                }
                // Update only the feedback label with fresh timestamp (no full refresh to avoid scroll jump)
                if (lblComplianceFeedback != null && complianceManager != null && selectedHousehold != null) {
                    com.ka4piece.model.ComplianceRecord freshRecord = complianceManager.getCurrentStatus(selectedHousehold.getHouseholdId());
                    if (freshRecord != null && freshRecord.getRecordedAt() != null) {
                        lblComplianceFeedback.setText("Compliance Record last updated on: " + formatRecordedAtPHT(freshRecord.getRecordedAt()));
                        lblComplianceFeedback.setTextFill(javafx.scene.paint.Color.web("#64748B"));
                    }
                }
            });
            delay.play();

        } catch (Exception e) {
            System.err.println("[OfficialDashboardController] Error recording compliance: " + e.getMessage());
            e.printStackTrace();
            // Re-enable button on error so the user can retry
            if (btnRecordCompliance != null) {
                btnRecordCompliance.setDisable(false);
            }
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
        int maxTotal = selectedHousehold != null
                ? selectedHousehold.getElemCount() + selectedHousehold.getJhsCount() + selectedHousehold.getShsCount()
                : 0;
        if (currentCount < maxTotal) {
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

    /**
     * Distributes a total compliant-children count across tiers using SHS-first precedence.
     * SHS gets filled first (highest grant), then JHS, then Elementary.
     * Counts are capped by household composition.
     *
     * @return int[3] where [0]=elem, [1]=jhs, [2]=shs
     */
    private int[] distributeTiersBySHSPrecedence(int totalCount) {
        int shsCap  = selectedHousehold != null ? selectedHousehold.getShsCount()  : 0;
        int jhsCap  = selectedHousehold != null ? selectedHousehold.getJhsCount()  : 0;
        int elemCap = selectedHousehold != null ? selectedHousehold.getElemCount() : 0;

        int remaining = totalCount;
        int shs  = Math.min(remaining, shsCap);  remaining -= shs;
        int jhs  = Math.min(remaining, jhsCap);  remaining -= jhs;
        int elem = Math.min(remaining, elemCap);

        return new int[]{elem, jhs, shs};
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

    private String formatRecordedAtPHT(java.sql.Timestamp timestamp) {
        java.time.LocalDateTime localDateTime = timestamp.toLocalDateTime();
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy - HH:mm");
        return localDateTime.format(formatter);
    }

    private void handleFormChange() {
        if (selectedHousehold == null || complianceManager == null) {
            return;
        }

        boolean pregnancyCare = chkPregnancyCare != null && chkPregnancyCare.isSelected();
        boolean healthCheckup = chkHealthCheckup != null && chkHealthCheckup.isSelected();
        boolean deworming = chkDeworming != null && chkDeworming.isSelected();
        boolean daycare = chkDaycare != null && chkDaycare.isSelected();
        boolean schoolAttendance = chkSchoolAttendance != null && chkSchoolAttendance.isSelected();
        boolean fds = chkFDS != null && chkFDS.isSelected();

        // Distribute count using SHS-first tier precedence
        int[] tiers = distributeTiersBySHSPrecedence(getCurrentCount());
        int elem = tiers[0];
        int jhs  = tiers[1];
        int shs  = tiers[2];

        com.ka4piece.model.ComplianceRecord tempRecord = new com.ka4piece.model.ComplianceRecord(
                selectedHousehold.getHouseholdId(),
                java.time.YearMonth.now().toString(),
                pregnancyCare,
                healthCheckup,
                deworming,
                daycare,
                schoolAttendance,
                fds,
                elem,
                jhs,
                shs,
                null
        );

        try {
            GrantBreakdown breakdown = complianceManager.calculateGrantBreakdown(tempRecord, selectedHousehold);
            updateGrantBreakdownUI(breakdown);
        } catch (Exception e) {
            System.err.println("Error calculating dynamic grant breakdown: " + e.getMessage());
        }
    }

    private void updateGrantBreakdownUI(GrantBreakdown breakdown) {
        if (lblHealthGrant != null) {
            lblHealthGrant.setText(String.format("₱%,.2f", breakdown.getHealthGrantAmount()));
        }
        if (lblEducationGrant != null) {
            lblEducationGrant.setText(String.format("₱%,.2f", breakdown.getEducationGrantAmount()));
        }
        if (lblRiceSubsidy != null) {
            lblRiceSubsidy.setText(String.format("₱%,.2f", breakdown.getRiceSubsidyAmount()));
        }
        if (lblTotalGrant != null) {
            lblTotalGrant.setText(String.format("TOTAL ELIGIBLE:  ₱%,.2f", breakdown.getTotalAmount()));
        }
        if (lblGrantStatus != null) {
            // checks if there are withheld reasons added except household has 3 or more eligible children (exceeding 3 children still eligible)
            if (breakdown.getWithheldReasons() != null && !breakdown.getWithheldReasons().isEmpty() && !breakdown.getWithheldReasons().contains("Household has more than 3 eligible children meeting attendance this month — grant computed for the 3 highest-tier children (Senior High prioritized, then Junior High, then Elementary), per system policy since DSWD guidance does not specify precedence.")) {
                lblGrantStatus.setText("• WITHHELD");
                lblGrantStatus.getStyleClass().setAll("badge-gray");
            } else {
                lblGrantStatus.setText("• ELIGIBLE");
                lblGrantStatus.getStyleClass().setAll("badge-active");
            }
        }
    }

    private void populateComplianceHistory(com.ka4piece.model.Household h) {
        if (tblHistory == null || complianceManager == null || h == null) return;

        List<com.ka4piece.model.ComplianceRecord> records = complianceManager.getComplianceHistory(h.getHouseholdId());
        javafx.collections.ObservableList<ComplianceHistoryRecord> historyList = javafx.collections.FXCollections.observableArrayList();

        for (com.ka4piece.model.ComplianceRecord r : records) {
            boolean healthPregnancy = !h.isHasPregnantMember() || r.isPregnancyCareStatus();
            boolean health0to5 = !h.isHas0to5Member() || r.isChild0to5HealthStatus();
            boolean hasChildren = (h.getElemCount() > 0 || h.getJhsCount() > 0 || h.getShsCount() > 0);
            boolean healthDeworming = !hasChildren || r.isDewormingStatus();
            boolean healthCompliant = healthPregnancy && health0to5 && healthDeworming;

            boolean educationCompliant = r.isDaycareAttendanceStatus() && r.isSchoolAttendanceStatus();
            boolean fdsCompliant = r.isFdsAttendanceStatus();
            boolean fullyCompliant = healthCompliant && educationCompliant && fdsCompliant;

            String healthStr = healthCompliant ? "Compliant" : "Non-Compliant";
            String eduStr = educationCompliant ? "Compliant" : "Non-Compliant";
            String fdsStr = fdsCompliant ? "Compliant" : "Non-Compliant";
            String statusStr = fullyCompliant ? "Compliant" : "Non-Compliant";

            historyList.add(new ComplianceHistoryRecord(
                r.getMonthYear(),
                healthStr,
                eduStr,
                fdsStr,
                statusStr
            ));
        }

        tblHistory.setItems(historyList);
    }

    // --- JOB VACANCY HELPER METHODS ---

    private void populatePostedVacancies() {
        if (boxPostedVacancies == null || jobMatchManager == null) return;
        boxPostedVacancies.getChildren().clear();

        List<Vacancy> vacancies = jobMatchManager.getAllVacancies().stream()
                .sorted((a, b) -> {
                    boolean aArch = "ARCHIVED".equalsIgnoreCase(a.getStatus());
                    boolean bArch = "ARCHIVED".equalsIgnoreCase(b.getStatus());
                    return Boolean.compare(aArch, bArch);
                })
                .collect(Collectors.toList());
        for (Vacancy v : vacancies) {
            VBox itemBox = new VBox();
            itemBox.setSpacing(2.0);
            
            if ("ARCHIVED".equalsIgnoreCase(v.getStatus())) {
                itemBox.setOpacity(0.55);
            }

            // Highlight selected vacancy
            if (selectedVacancy != null && selectedVacancy.getVacancyId().equals(v.getVacancyId())) {
                itemBox.getStyleClass().add("list-item-active");
            } else {
                itemBox.getStyleClass().add("list-item");
            }

            Label titleLabel = new Label(v.getTitle() != null ? v.getTitle() : "Unnamed Job");
            if (selectedVacancy != null && selectedVacancy.getVacancyId().equals(v.getVacancyId())) {
                titleLabel.getStyleClass().add("list-item-title-active");
            } else {
                titleLabel.getStyleClass().add("list-item-title");
            }

            String subText = (v.getLocation() != null ? v.getLocation() : "No Location") + " • " + (v.getType() != null ? v.getType() : "Full-time");
            if ("ARCHIVED".equalsIgnoreCase(v.getStatus())) {
                subText += " (ARCHIVED)";
            }
            Label subLabel = new Label(subText);
            subLabel.getStyleClass().add("list-item-sub");

            itemBox.getChildren().addAll(titleLabel, subLabel);
            itemBox.setOnMouseClicked(e -> selectVacancy(v));
            boxPostedVacancies.getChildren().add(itemBox);
        }
    }

    private void selectVacancy(Vacancy v) {
        if (v == null) return;
        this.selectedVacancy = v;

        // Refresh lists and details
        populatePostedVacancies();

        if (lblSelectedVacancyTitle != null) {
            lblSelectedVacancyTitle.setText(v.getTitle());
        }
        if (lblSelectedVacancyDescription != null) {
            lblSelectedVacancyDescription.setText(v.getDescription() != null && !v.getDescription().isEmpty()
                    ? v.getDescription()
                    : "No job description provided.");
        }
        if (lblSelectedVacancyLocTypeSalary != null) {
            String locTypeSalary = "📍 Location: " + (v.getLocation() != null ? v.getLocation() : "N/A") + "\n" +
                                   "💼 Type: " + (v.getType() != null ? v.getType() : "N/A") + "\n" +
                                   "💰 Salary: " + (v.getCompensation() != null ? v.getCompensation() : "N/A");
            if (v.getStatus() != null && v.getStatus().equals("ARCHIVED")) {
                locTypeSalary += " (ARCHIVED: " + (v.getArchiveReason() != null ? v.getArchiveReason() : "Filled") + ")";
            }
            lblSelectedVacancyLocTypeSalary.setText(locTypeSalary);
        }
        if (lblSelectedVacancyEducationExperience != null) {
            String eduExp = "🎓 Education: " + (v.getEducationRequirement() != null ? v.getEducationRequirement() : "N/A") + "\n" +
                            "⏳ Experience: " + v.getExperienceYearsRequired() + " Year(s)";
            lblSelectedVacancyEducationExperience.setText(eduExp);
        }
        if (lblSelectedVacancySkills != null) {
            String skills = "🛠️ Skills:\n" +
                            ((v.getSkillRequirements() != null && !v.getSkillRequirements().isEmpty())
                                    ? String.join(", ", v.getSkillRequirements())
                                    : "None specified");
            lblSelectedVacancySkills.setText(skills);
        }

        populateRankedApplicants(v.getVacancyId());
    }

    private void populateRankedApplicants(String vacancyId) {
        if (boxRankedApplicants == null || jobMatchManager == null) return;
        boxRankedApplicants.getChildren().clear();

        List<RankedCandidate> candidates = jobMatchManager.getRankedApplicants(vacancyId);
        if (candidates.isEmpty()) {
            Label noApplicantsLabel = new Label("No applicants for this vacancy yet.");
            noApplicantsLabel.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic; -fx-padding: 15px;");
            boxRankedApplicants.getChildren().add(noApplicantsLabel);
            return;
        }

        for (RankedCandidate c : candidates) {
            GridPane row = new GridPane();
            row.getStyleClass().add("table-row");
            
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPercentWidth(40.0);
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPercentWidth(30.0);
            ColumnConstraints col3 = new ColumnConstraints();
            col3.setPercentWidth(30.0);
            row.getColumnConstraints().addAll(col1, col2, col3);

            Label nameLabel = new Label(c.getProfile().getMemberName());
            nameLabel.setTextFill(javafx.scene.paint.Color.web("#1e293b"));
            nameLabel.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 13));

            Label scoreLabel = new Label((int)(c.getScore() * 100) + "%");
            scoreLabel.getStyleClass().add("badge-active");

            ComboBox<String> cmbStatus = new ComboBox<>();
            cmbStatus.getItems().addAll("APPLIED", "FOR_INTERVIEW", "REJECTED", "HIRED");
            cmbStatus.setValue(c.getStatus());
            cmbStatus.setMaxWidth(Double.MAX_VALUE);
            cmbStatus.getStyleClass().add("search-field");

            cmbStatus.setOnAction(e -> {
                String newStatus = cmbStatus.getValue();
                if (newStatus != null) {
                    try {
                        jobMatchManager.updateApplicationStatus(c.getProfile().getJobseekerId(), vacancyId, newStatus);
                        Vacancy updatedVacancy = jobMatchManager.getVacancyDetail(vacancyId);
                        selectVacancy(updatedVacancy);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            row.add(nameLabel, 0, 0);
            row.add(scoreLabel, 1, 0);
            row.add(cmbStatus, 2, 0);

            boxRankedApplicants.getChildren().add(row);
        }
    }

    @FXML
    private void handleEditVacancy() {
        if (selectedVacancy == null) return;
        // Populate form with selected vacancy data
        if (txtJobTitle != null)           txtJobTitle.setText(selectedVacancy.getTitle() != null ? selectedVacancy.getTitle() : "");
        if (cmbEducationRequirement != null) cmbEducationRequirement.setValue(selectedVacancy.getEducationRequirement());
        if (txtSkills != null && selectedVacancy.getSkillRequirements() != null)
            txtSkills.setText(String.join(", ", selectedVacancy.getSkillRequirements()));
        if (txtJobDescription != null)     txtJobDescription.setText(selectedVacancy.getDescription() != null ? selectedVacancy.getDescription() : "");
        if (txtExperience != null)         txtExperience.setText(String.valueOf(selectedVacancy.getExperienceYearsRequired()));
        if (txtLocation != null)           txtLocation.setText(selectedVacancy.getLocation() != null ? selectedVacancy.getLocation() : "");
        if (txtCompensation != null)       txtCompensation.setText(selectedVacancy.getCompensation() != null ? selectedVacancy.getCompensation() : "");
        if (cmbEmploymentType != null)     cmbEmploymentType.setValue(selectedVacancy.getType());

        // Switch form to EDIT mode
        vacancyEditModeId = selectedVacancy.getVacancyId();
        if (btnEnterVacancy != null) btnEnterVacancy.setText("UPDATE VACANCY");
        if (btnCancelEdit != null) { btnCancelEdit.setVisible(true); btnCancelEdit.setManaged(true); }
        if (lblVacancyFormFeedback != null) { lblVacancyFormFeedback.setText(""); lblVacancyFormFeedback.setStyle(""); }
    }

    private void cancelEditMode() {
        vacancyEditModeId = null;
        if (btnEnterVacancy != null) btnEnterVacancy.setText("ENTER VACANCY");
        if (btnCancelEdit != null) { btnCancelEdit.setVisible(false); btnCancelEdit.setManaged(false); }
        if (lblVacancyFormFeedback != null) { lblVacancyFormFeedback.setText(""); lblVacancyFormFeedback.setStyle(""); }
        if (txtJobTitle != null)           txtJobTitle.clear();
        if (cmbEducationRequirement != null) cmbEducationRequirement.setValue(null);
        if (txtSkills != null)             txtSkills.clear();
        if (txtJobDescription != null)     txtJobDescription.clear();
        if (txtExperience != null)         txtExperience.clear();
        if (txtLocation != null)           txtLocation.clear();
        if (txtCompensation != null)       txtCompensation.clear();
        if (cmbEmploymentType != null)     cmbEmploymentType.setValue(null);
    }

    private void handleEnterVacancy() {
        if (jobMatchManager == null) return;

        String title = txtJobTitle.getText().trim();
        String education = cmbEducationRequirement.getValue();
        String skillsText = txtSkills.getText().trim();
        String description = txtJobDescription.getText().trim();
        String experienceStr = txtExperience.getText().trim();
        String location = txtLocation.getText().trim();
        String compensation = txtCompensation.getText().trim();
        String type = cmbEmploymentType.getValue();

        if (title.isEmpty() || education == null || skillsText.isEmpty() || location.isEmpty() || type == null) {
            if (lblVacancyFormFeedback != null) {
                lblVacancyFormFeedback.setText("Please fill in all required fields.");
                lblVacancyFormFeedback.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
            }
            return;
        }

        int experience = 0;
        try {
            if (!experienceStr.isEmpty()) {
                experience = Integer.parseInt(experienceStr);
            }
        } catch (NumberFormatException e) {
            if (lblVacancyFormFeedback != null) {
                lblVacancyFormFeedback.setText("Experience must be a number.");
                lblVacancyFormFeedback.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
            }
            return;
        }

        try {
            if (vacancyEditModeId != null) {
                // UPDATE existing vacancy
                List<String> skills = Arrays.stream(skillsText.split("[,;]"))
                        .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                jobMatchManager.getJobMatchRepository().updateVacancyDetails(
                        vacancyEditModeId, title, description, education, skills, experience, location, compensation, type
                );
                selectedVacancy = jobMatchManager.getVacancyDetail(vacancyEditModeId);
                if (lblVacancyFormFeedback != null) {
                    lblVacancyFormFeedback.setText("✓ Vacancy updated successfully.");
                    lblVacancyFormFeedback.setStyle("-fx-text-fill: #15803D; -fx-font-weight: bold;");
                }
                cancelEditMode();
                selectVacancy(selectedVacancy);
            } else {
                // CREATE new vacancy
                String officialId = Session.getInstance() != null ? Session.getInstance().getUserId() : null;
                List<String> skills = Arrays.stream(skillsText.split("[,;]"))
                        .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                Vacancy newVacancy = new Vacancy(null, title, description, education, location, compensation, type, skills, experience, officialId);
                jobMatchManager.createVacancy(newVacancy);
                txtJobTitle.clear();
                cmbEducationRequirement.setValue(null);
                txtSkills.clear();
                txtJobDescription.clear();
                txtExperience.clear();
                txtLocation.clear();
                txtCompensation.clear();
                cmbEmploymentType.setValue(null);
                if (lblVacancyFormFeedback != null) {
                    lblVacancyFormFeedback.setText("✓ Vacancy posted successfully.");
                    lblVacancyFormFeedback.setStyle("-fx-text-fill: #15803D; -fx-font-weight: bold;");
                }
                populatePostedVacancies();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            if (lblVacancyFormFeedback != null) {
                lblVacancyFormFeedback.setText("✗ Error: " + ex.getMessage());
                lblVacancyFormFeedback.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
            }
        }
    }
}