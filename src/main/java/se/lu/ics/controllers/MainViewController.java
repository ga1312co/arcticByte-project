package se.lu.ics.controllers;

import java.io.IOException;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.input.MouseEvent;
import se.lu.ics.Main;
import se.lu.ics.data.ConnectionHandler;
import se.lu.ics.data.DaoException;
import se.lu.ics.data.ExcelOpener;
import se.lu.ics.data.MetaDataDao;
import se.lu.ics.data.WorkDao;
import se.lu.ics.models.Employee;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;

public class MainViewController {

    @FXML
    private AnchorPane anchorPaneHome;

    @FXML
    private ImageView imageViewExcelLogo;

    @FXML
    private ComboBox<String> comboBoxMetaData;

    @FXML
    private TableView<String> tableViewMetaData;

    @FXML
    private TableColumn<String, String> columnMetaData;

    @FXML
    private TableView<String> tableViewEmpOfTheMonth;

    @FXML
    private TableColumn<String, String> columnEmpOfTheMonth;

    /* NAVIGATION BAR */

    @FXML
    private void handleHomeClick(MouseEvent event) throws IOException {
        Main.setScene("MainView");
    }

    @FXML
    private void handleProjectsClick(MouseEvent event) throws IOException {
        Main.setScene("ProjectView");
    }

    @FXML
    private void handleMilestonesClick(MouseEvent event) throws IOException {
        Main.setScene("MilestoneView");
    }

    @FXML
    private void handleConsultantsClick(MouseEvent event) throws IOException {
        Main.setScene("EmployeeView");
    }

    @FXML
    private void handleWorkClick(MouseEvent event) throws IOException {
        Main.setScene("WorkView");
    }

    // Initialize method

    @FXML
    public void initialize() {

        // Set the items of the ComboBox
        comboBoxMetaData.getItems().addAll(
                "Column names",
                "Primary key constraints",
                "Check constraints",
                "Employee columns except INT",
                "Table with most rows");

        // Add listener to ComboBox selection changes
        comboBoxMetaData.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                handleComboBoxMetaData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Call the method to show the hardest working employee
        try {
            showHardestWorkingEmployee();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // SHOW ALERT METHOD
    public static void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // OPEN EXCEL METHOD

    @FXML
    private void handleOpenExcel(MouseEvent event) {
        // Instantiate ExcelOpener locally in the method
        ExcelOpener excelOpener = new ExcelOpener();
        String filePath = "src/main/resources/ArcticByte.xlsx";
        excelOpener.openExcelFile(filePath);
    }

    // HANDLE COMBOBOX WITH METADATA
    
    @FXML
    private void handleComboBoxMetaData() throws IOException {
        MetaDataDao metaDataDao = new MetaDataDao();
        try {
            String selectedOption = comboBoxMetaData.getValue();
            List<String> dataList = null;

            switch (selectedOption) { // Switch statement to handle different selections
                case "Column names":
                    dataList = metaDataDao.getColumnNames();
                    break;
                case "Primary key constraints":
                    dataList = metaDataDao.getPKConstraints();
                    break;
                case "Check constraints":
                    dataList = metaDataDao.getCheckConstraints();
                    break;
                case "Employee columns except INT":
                    dataList = metaDataDao.getNonIntegerColumns();
                    break;
                case "Table with most rows":
                    dataList = metaDataDao.getTableWithMostRows();
                    break;
                default:
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid selection.");
                    return;
            }

            // Set the cell value factory and items of the TableView
            columnMetaData.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
            tableViewMetaData.setItems(FXCollections.observableArrayList(dataList));

        } catch (DaoException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    // SHOW HARDEST WORKING EMPLOYEE

    @FXML
    private void showHardestWorkingEmployee() throws IOException {
        try {
            WorkDao workDao = new WorkDao(new ConnectionHandler());
            Employee hardestWorkingConsultant = workDao.getHardestWorkingConsultant();

            columnEmpOfTheMonth.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
            tableViewEmpOfTheMonth.setItems(FXCollections.observableArrayList(hardestWorkingConsultant.toString()));
        } catch (DaoException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

}
