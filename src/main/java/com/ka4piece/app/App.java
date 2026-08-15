package com.ka4piece.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the login.fxml file from your resources/controller path
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/ka4piece/controller/login.fxml")
            );

            Parent root = loader.load();

            // Set up the scene with dimensions (matching prefHeight=720, prefWidth=1280)
            Scene scene = new Scene(root, 1280, 720);

            // Configure the primary stage/window
            primaryStage.setTitle("KaAyuda - Barangay 4Ps Compliance System");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load login.fxml. Verify the resource path and CSS/image locations.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}