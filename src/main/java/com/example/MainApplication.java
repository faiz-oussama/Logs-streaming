package com.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.control.Label;

public class MainApplication extends Application {
    private Parent logVisRoot;
    private Parent analyticsRoot;
    private Parent filterRoot;
    private LogVisController logVisController;
    private LogAnalyticsController analyticsController;
    private FilterController filterController;
    private StackPane mainContainer;
    
    private static MainApplication instance;
    
    public MainApplication() {
        instance = this;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create main container
        mainContainer = new StackPane();
        
        // Load Log Visualization view
        FXMLLoader logVisLoader = new FXMLLoader(getClass().getResource("/logVis.fxml"));
        logVisRoot = logVisLoader.load();
        logVisController = logVisLoader.getController();
        
        // Load Log Analytics view
        FXMLLoader analyticsLoader = new FXMLLoader(getClass().getResource("/logAnalytics.fxml"));
        analyticsRoot = analyticsLoader.load();
        analyticsController = analyticsLoader.getController();

        // Load Filter view
        FXMLLoader filterLoader = new FXMLLoader(getClass().getResource("/filter.fxml"));
        filterRoot = filterLoader.load();
        filterController = filterLoader.getController();
        
        // Connect the controllers
        logVisController.setAnalyticsController(analyticsController);
        
        // Set up view switching
        setupViewSwitching();
        
        // Initially show log visualization view
        mainContainer.getChildren().add(logVisRoot);
        
        // Create scene
        Scene scene = new Scene(mainContainer, 1280, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        // Set up stage
        primaryStage.setTitle("Log Management System");
        primaryStage.setScene(scene);
        
        // Add shutdown hook
        primaryStage.setOnCloseRequest(event -> {
            // Properly shutdown all components
            logVisController.shutdown();
            Platform.exit();
            System.exit(0);
        });
        
        primaryStage.show();
        
        // Start log streaming
        logVisController.startLogStreaming();
    }

    private void setupViewSwitching() {
        // Get navbar items from LogVisController
        Label dashboardLabel = logVisController.getDashboardLabel();
        Label logsLabel = logVisController.getLogsLabel();
        Label filterLabel = logVisController.getFilterLabel();
        
        // Add click handlers
        dashboardLabel.setOnMouseClicked(event -> {
            try {
                setRoot("logAnalytics");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            
            // Update active states
            dashboardLabel.getStyleClass().add("nav-item-active");
            logsLabel.getStyleClass().remove("nav-item-active");
            filterLabel.getStyleClass().remove("nav-item-active");
        });
        
        logsLabel.setOnMouseClicked(event -> {
            try {
                setRoot("logVis");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            
            // Update active states
            logsLabel.getStyleClass().add("nav-item-active");
            dashboardLabel.getStyleClass().remove("nav-item-active");
            filterLabel.getStyleClass().remove("nav-item-active");
        });

        filterLabel.setOnMouseClicked(event -> {
            try {
                setRoot("filter");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            
            // Update active states
            filterLabel.getStyleClass().add("nav-item-active");
            dashboardLabel.getStyleClass().remove("nav-item-active");
            logsLabel.getStyleClass().remove("nav-item-active");
        });
    }

    public static void setRoot(String viewName) throws Exception {
        if (instance == null) {
            throw new IllegalStateException("MainApplication not initialized");
        }
        
        instance.mainContainer.getChildren().clear();
        
        switch (viewName) {
            case "logVis":
                instance.mainContainer.getChildren().add(instance.logVisRoot);
                break;
            case "logAnalytics":
                instance.mainContainer.getChildren().add(instance.analyticsRoot);
                break;
            case "filter":
                instance.mainContainer.getChildren().add(instance.filterRoot);
                break;
            default:
                throw new IllegalArgumentException("Unknown view: " + viewName);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}