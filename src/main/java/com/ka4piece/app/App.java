package com.ka4piece.app;

import com.ka4piece.controller.*;
import com.ka4piece.manager.*;
import com.ka4piece.repository.*;
import com.ka4piece.strategy.*;

import javafx.application.Application;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class App extends Application {

    private static DbConfig dbConfig;
    private static AuthRepository authRepository;
    private static ComplianceRepository complianceRepository;
    private static JobMatchRepository jobMatchRepository;

    private static AuthManager authManager;
    private static ComplianceManager complianceManager;
    private static JobMatchManager jobMatchManager;

    private static MatchStrategy matchStrategy;

    @Override
    public void init() throws Exception {
        dbConfig = new DbConfig("db.properties");
        authRepository = new AuthRepository(dbConfig.getJdbcurl(), dbConfig.getUsername(), dbConfig.getPassword());
        complianceRepository = new ComplianceRepository(dbConfig.getJdbcurl(), dbConfig.getUsername(), dbConfig.getPassword());
        jobMatchRepository = new JobMatchRepository(dbConfig.getJdbcurl(), dbConfig.getUsername(), dbConfig.getPassword());

        authManager = new AuthManager(authRepository);
        complianceManager = new ComplianceManager(complianceRepository, authRepository);
        matchStrategy = new WeightedMatchStrategy();
        jobMatchManager = new JobMatchManager(jobMatchRepository, matchStrategy);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = loadFXML("/view/login.fxml");
            Scene scene = new Scene(root, 1280, 720);
            primaryStage.setTitle("KaAyuda - Barangay 4Ps Compliance System");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load initial view. Verify the resource path and CSS/image locations.");
        }
    }

    public static Parent loadFXML(String fxmlPath) throws IOException {
        URL resource = App.class.getResource(fxmlPath);
        if (resource == null) {
            resource = App.class.getResource("/view" + fxmlPath);
        }
        if (resource == null) {
            throw new IOException("Could not find view file: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(resource);
        loader.setControllerFactory(clazz -> {
            if (clazz == LoginController.class) {
                return new LoginController(authRepository);
            } else if (clazz == OfficialDashboardController.class) {
                return new OfficialDashboardController(authManager, complianceManager, jobMatchManager);
            } else if (clazz == ViewProfileController.class) {
                return new ViewProfileController(authRepository);
            } else if (clazz == BeneficiaryDashboardController.class) {
                return new BeneficiaryDashboardController(authRepository, complianceManager);
            } else if (clazz == ForgotPasswordController.class) {
                return new ForgotPasswordController(authRepository);
            }
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to construct controller: " + clazz.getName(), e);
            }
        });
        return loader.load();
    }

    public static void switchScene(Event event, String fxmlPath) {
        try {
            Parent root = loadFXML(fxmlPath);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to switch scene to: " + fxmlPath);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}