package com.example;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.List;

public class FilterController implements Initializable {
    @FXML private ComboBox<String> timePresetCombo;
    @FXML private DatePicker startDate;
    @FXML private DatePicker endDate;
    @FXML private TextField startTime;
    @FXML private TextField endTime;
    
    @FXML private CheckBox errorCheck;
    @FXML private CheckBox warnCheck;
    @FXML private CheckBox infoCheck;
    @FXML private CheckBox debugCheck;
    @FXML private CheckBox traceCheck;
    
    @FXML private ComboBox<String> sourceCombo;
    @FXML private ComboBox<String> componentCombo;
    
    @FXML private VBox advancedFiltersContainer;
    @FXML private Button resetBtn;
    @FXML private Button applyBtn;
    
    @FXML private Label dashboardLabel;
    @FXML private Label logsLabel;
    @FXML private Label filterLabel;

    @FXML private TableView<LogEntry> filteredLogsTable;
    @FXML private TableColumn<LogEntry, String> columnIP;
    @FXML private TableColumn<LogEntry, String> columnTimestamp;
    @FXML private TableColumn<LogEntry, String> columnRequestType;
    @FXML private TableColumn<LogEntry, String> columnEndpoint;
    @FXML private TableColumn<LogEntry, String> columnStatusCode;
    @FXML private TableColumn<LogEntry, String> columnResponseTime;
    
    private List<HBox> advancedFilterRows = new ArrayList<>();
    private ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTimeControls();
        setupLogLevelControls();
        setupSourceControls();
        setupAdvancedFilters();
        setupButtons();
        setupNavigation();
        setupTable();
        
        // Add listener to timePresetCombo to handle custom range selection
        timePresetCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isCustomRange = "Custom range".equals(newVal);
            enableCustomTimeRange(isCustomRange);
        });
        
        // Initially disable custom time range controls
        enableCustomTimeRange(false);
    }
    
    private void setupTimeControls() {
        timePresetCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                switch (newVal) {
                    case "Last 15 minutes":
                        setTimeRange(15);
                        break;
                    case "Last hour":
                        setTimeRange(60);
                        break;
                    case "Last 24 hours":
                        setTimeRange(24 * 60);
                        break;
                    case "Last 7 days":
                        setTimeRange(7 * 24 * 60);
                        break;
                    case "Custom range":
                        enableCustomTimeRange(true);
                        break;
                }
            }
        });
        
        // Set default time format
        startTime.setPromptText("HH:mm");
        endTime.setPromptText("HH:mm");
    }
    
    private void setTimeRange(int minutes) {
        LocalDate now = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        
        endDate.setValue(now);
        endTime.setText(currentTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        
        if (minutes < 24 * 60) {
            startDate.setValue(now);
            startTime.setText(currentTime.minusMinutes(minutes).format(DateTimeFormatter.ofPattern("HH:mm")));
        } else {
            startDate.setValue(now.minusDays(minutes / (24 * 60)));
            startTime.setText(currentTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        }
    }
    
    private void setupLogLevelControls() {
        // Set default selections
        infoCheck.setSelected(true);
        errorCheck.setSelected(true);
        warnCheck.setSelected(true);
    }
    
    private void setupSourceControls() {
        sourceCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateComponentCombo(newVal);
            }
        });
    }
    
    private void updateComponentCombo(String source) {
        ObservableList<String> components = FXCollections.observableArrayList();
        switch (source) {
            case "Application":
                components.addAll("Frontend", "Backend", "Database", "Cache");
                break;
            case "System":
                components.addAll("OS", "Hardware", "Services", "Drivers");
                break;
            case "Security":
                components.addAll("Authentication", "Authorization", "Firewall", "Encryption");
                break;
            case "Network":
                components.addAll("HTTP", "TCP/IP", "DNS", "Load Balancer");
                break;
        }
        componentCombo.setItems(components);
    }
    
    private void setupAdvancedFilters() {
        // Add initial filter row
        addFilterRow();
    }
    
    private void addFilterRow() {
        HBox filterRow = (HBox) advancedFiltersContainer.getChildren().get(0);
        Button addButton = (Button) filterRow.getChildren().get(filterRow.getChildren().size() - 1);
        
        addButton.setOnAction(e -> {
            HBox newRow = createFilterRow();
            advancedFiltersContainer.getChildren().add(newRow);
            advancedFilterRows.add(newRow);
        });
    }
    
    private HBox createFilterRow() {
        HBox row = new HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.getStyleClass().add("advanced-filter-row");
        
        // Add components similar to the first row
        ComboBox<String> fieldCombo = new ComboBox<>();
        fieldCombo.setPromptText("Field");
        fieldCombo.setItems(FXCollections.observableArrayList(
            "Message", "User ID", "IP Address", "Session ID"
        ));
        
        ComboBox<String> operatorCombo = new ComboBox<>();
        operatorCombo.setPromptText("Operator");
        operatorCombo.setItems(FXCollections.observableArrayList(
            "Contains", "Equals", "Starts with", "Ends with", "Regex"
        ));
        
        TextField valueField = new TextField();
        valueField.setPromptText("Value");
        
        Button removeButton = new Button("-");
        removeButton.getStyleClass().add("remove-filter-btn");
        removeButton.setOnAction(e -> advancedFiltersContainer.getChildren().remove(row));
        
        row.getChildren().addAll(fieldCombo, operatorCombo, valueField, removeButton);
        return row;
    }
    
    private void setupButtons() {
        resetBtn.setOnAction(e -> resetFilters());
        applyBtn.setOnAction(e -> applyFilters());
    }
    
    private void resetFilters() {
        timePresetCombo.getSelectionModel().clearSelection();
        startDate.setValue(null);
        endDate.setValue(null);
        startTime.clear();
        endTime.clear();
        
        errorCheck.setSelected(false);
        warnCheck.setSelected(false);
        infoCheck.setSelected(false);
        debugCheck.setSelected(false);
        traceCheck.setSelected(false);
        
        sourceCombo.getSelectionModel().clearSelection();
        componentCombo.getSelectionModel().clearSelection();
        
        // Clear advanced filters except the first row
        while (advancedFiltersContainer.getChildren().size() > 1) {
            advancedFiltersContainer.getChildren().remove(1);
        }
    }
    
    private void applyFilters() {
        // Get the current filter values
        LocalDate startDateValue = startDate.getValue();
        LocalDate endDateValue = endDate.getValue();
        String startTimeValue = startTime.getText();
        String endTimeValue = endTime.getText();
        
        // Get selected log levels
        List<String> selectedLevels = new ArrayList<>();
        if (errorCheck.isSelected()) selectedLevels.add("ERROR");
        if (warnCheck.isSelected()) selectedLevels.add("WARN");
        if (infoCheck.isSelected()) selectedLevels.add("INFO");
        if (debugCheck.isSelected()) selectedLevels.add("DEBUG");
        if (traceCheck.isSelected()) selectedLevels.add("TRACE");
        
        // Get source and component
        String selectedSource = sourceCombo.getValue();
        String selectedComponent = componentCombo.getValue();
        
        // Get advanced filters
        List<AdvancedFilter> filters = new ArrayList<>();
        for (HBox row : advancedFilterRows) {
            ComboBox<String> fieldCombo = (ComboBox<String>) row.getChildren().get(0);
            ComboBox<String> operatorCombo = (ComboBox<String>) row.getChildren().get(1);
            TextField valueField = (TextField) row.getChildren().get(2);
            
            if (fieldCombo.getValue() != null && operatorCombo.getValue() != null && !valueField.getText().isEmpty()) {
                filters.add(new AdvancedFilter(
                    fieldCombo.getValue(),
                    operatorCombo.getValue(),
                    valueField.getText()
                ));
            }
        }
        
        // TODO: Apply the filters to your log data
        System.out.println("Applying filters:");
        System.out.println("Time range: " + startDateValue + " " + startTimeValue + " to " + endDateValue + " " + endTimeValue);
        System.out.println("Log levels: " + selectedLevels);
        System.out.println("Source: " + selectedSource + ", Component: " + selectedComponent);
        System.out.println("Advanced filters: " + filters);
    }
    
    private void setupTable() {
        // Set up the table columns
        columnIP.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        columnTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        columnRequestType.setCellValueFactory(new PropertyValueFactory<>("requestType"));
        columnEndpoint.setCellValueFactory(new PropertyValueFactory<>("endpoint"));
        columnStatusCode.setCellValueFactory(new PropertyValueFactory<>("statusCode"));
        columnResponseTime.setCellValueFactory(new PropertyValueFactory<>("responseTime"));

        // Bind the table items
        filteredLogsTable.setItems(logEntries);
    }
    
    private void setupNavigation() {
        dashboardLabel.setOnMouseClicked(event -> {
            try {
                MainApplication.setRoot("logAnalytics");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        logsLabel.setOnMouseClicked(event -> {
            try {
                MainApplication.setRoot("logVis");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    public void enableCustomTimeRange(boolean enable) {
        startDate.setDisable(!enable);
        endDate.setDisable(!enable);
        startTime.setDisable(!enable);
        endTime.setDisable(!enable);
    }
    
    // Inner class for advanced filters
    public static class AdvancedFilter {
        private String field;
        private String operator;
        private String value;
        
        public AdvancedFilter(String field, String operator, String value) {
            this.field = field;
            this.operator = operator;
            this.value = value;
        }
        
        @Override
        public String toString() {
            return field + " " + operator + " " + value;
        }
    }
    
    public static class LogEntry {
        private String ipAddress;
        private String timestamp;
        private String requestType;
        private String endpoint;
        private String statusCode;
        private String responseTime;
        
        public LogEntry(String ipAddress, String timestamp, String requestType, String endpoint, String statusCode, String responseTime) {
            this.ipAddress = ipAddress;
            this.timestamp = timestamp;
            this.requestType = requestType;
            this.endpoint = endpoint;
            this.statusCode = statusCode;
            this.responseTime = responseTime;
        }
        
        public String getIpAddress() { return ipAddress; }
        public String getTimestamp() { return timestamp; }
        public String getRequestType() { return requestType; }
        public String getEndpoint() { return endpoint; }
        public String getStatusCode() { return statusCode; }
        public String getResponseTime() { return responseTime; }
    }
}