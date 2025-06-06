package se.lu.ics.controllers;

import java.io.IOException;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import se.lu.ics.Main;
import se.lu.ics.data.ConnectionHandler;
import se.lu.ics.data.DaoException;
import se.lu.ics.data.EmployeeDao;
import se.lu.ics.data.ProjectDao;
import se.lu.ics.data.WorkDao;
import se.lu.ics.models.Employee;
import se.lu.ics.models.Project;
import se.lu.ics.models.Work;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class WorkViewController {

    @FXML
    private TableView<Work> tableViewDataShow;

    @FXML
    private ComboBox<Employee> comboBoxFilterEmployee;

    @FXML
    private ComboBox<Project> comboBoxFilterProject;

    @FXML
    private TableColumn<Work, String> columnConsultantName;
    @FXML
    private TableColumn<Work, String> columnProjectName;
    @FXML
    private TableColumn<Work, Integer> columnAssignedHours;
    @FXML
    private TableColumn<Work, Integer> columnHoursWorked;
    @FXML
    private Label responseLabel;
    @FXML
    private Button buttonEditWork;

    // Initialize method to populate ComboBoxes when the view is loaded
    @FXML
    public void initialize() throws IOException {
        populateEmployeeComboBox();
        populateProjectComboBox();

        // Set up table columns to display Work data
        columnConsultantName.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getEmployee().toString()));
        columnProjectName.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getProject().getProjectName()));
        columnAssignedHours.setCellValueFactory(new PropertyValueFactory<>("assignedHours"));
        columnHoursWorked.setCellValueFactory(new PropertyValueFactory<>("hoursWorked"));

        // Set default values in ComboBoxes
        comboBoxFilterEmployee.getSelectionModel().selectFirst();
        comboBoxFilterProject.getSelectionModel().selectFirst();

        // Update work table when ComboBox values are changed
        comboBoxFilterEmployee.setOnAction(event -> { 
            try {
                updateWorkTable();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        comboBoxFilterProject.setOnAction(event -> {
            try {
                updateWorkTable();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Ensure table updates when ComboBox values are changed
        comboBoxFilterEmployee.setOnShowing(event -> {
            try {
                // Update comboboxes
                populateEmployeeComboBox();
                populateProjectComboBox();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        comboBoxFilterProject.setOnShowing(event -> {
            try {
                // Update comboboxes
                populateEmployeeComboBox();
                populateProjectComboBox();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Populate the table when the view is first launched
        updateWorkTable();
    }

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

    // POPULATE EMPLOYEE COMBOBOX

    private void populateEmployeeComboBox() throws IOException {
        comboBoxFilterEmployee.getItems().clear();

        // Add a special Employee object representing "All Employees"
        comboBoxFilterEmployee.getItems().add(null); // null represents "All Employees"

        try {
            EmployeeDao employeeDao = new EmployeeDao(new ConnectionHandler()); // Create a new EmployeeDao object
            List<Employee> employees = employeeDao.getAllEmployees(); // Get all employees from the database
            comboBoxFilterEmployee.getItems().addAll(employees); // Add all employees to the ComboBox
        } catch (DaoException e) {
            e.printStackTrace();
        }

        comboBoxFilterEmployee.getSelectionModel().selectFirst(); // Select "All Employees"

        // Set cell factory to display Employee's name
        comboBoxFilterEmployee.setCellFactory(new Callback<ListView<Employee>, ListCell<Employee>>() { 
            @Override // Override the updateItem method
            public ListCell<Employee> call(ListView<Employee> param) { // Call the method with a ListView parameter
                return new ListCell<Employee>() { // Return a new ListCell object
                    @Override // Override the updateItem method
                    protected void updateItem(Employee item, boolean empty) { 
                        super.updateItem(item, empty); // Call the super method
                        if (empty || item == null) { // If the item is empty or null
                            setText("All Employees"); // Set the text to "All Employees"
                        } else {
                            setText(item.getEmpFirstName() + " " + item.getEmpLastName()); // Set the text to the employee's name
                        }
                    }
                };
            }
        });

        // Also set the button cell
        comboBoxFilterEmployee.setButtonCell(new ListCell<Employee>() {
            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("All Employees"); // Set the text to "All Employees"
                } else {
                    setText(item.getEmpFirstName() + " " + item.getEmpLastName()); // Set the text to the employee's name
                }
            }
        });
    }

    // POPULATE PROJECT COMBOBOX

    private void populateProjectComboBox() throws IOException {
        comboBoxFilterProject.getItems().clear();

        // Add a special Project object representing "All Projects"
        comboBoxFilterProject.getItems().add(null); // null represents "All Projects"

        try {
            ProjectDao projectDao = new ProjectDao(new ConnectionHandler()); // Create a new ProjectDao object
            List<Project> projects = projectDao.getAllProjects(); // Get all projects from the database
            comboBoxFilterProject.getItems().addAll(projects); // Add all projects to the ComboBox
        } catch (DaoException e) {
            e.printStackTrace();
        }

        comboBoxFilterProject.getSelectionModel().selectFirst(); // Select "All Projects"

        // Set cell factory to display Project's name
        comboBoxFilterProject.setCellFactory(new Callback<ListView<Project>, ListCell<Project>>() { // Set the cell factory
            @Override // Override the updateItem method
            public ListCell<Project> call(ListView<Project> param) { // Call the method with a ListView parameter
                return new ListCell<Project>() { // Return a new ListCell object
                    @Override // Override the updateItem method
                    protected void updateItem(Project item, boolean empty) { 
                        super.updateItem(item, empty); // Call the super method
                        if (empty || item == null) { // If the item is empty or null
                            setText("All Projects"); // Set the text to "All Projects"
                        } else { 
                            setText(item.getProjectName()); // Set the text to the project's name
                        }
                    }
                };
            }
        });

        // Also set the button cell
        comboBoxFilterProject.setButtonCell(new ListCell<Project>() { 
            @Override // Override the updateItem method
            protected void updateItem(Project item, boolean empty) { // Update the item
                super.updateItem(item, empty); // Call the super method
                if (empty || item == null) { // If the item is empty or null
                    setText("All Projects"); // Set the text to "All Projects"
                } else {
                    setText(item.getProjectName()); // Set the text to the project's name
                }
            }
        });
    }

    // UPDATE WORK TABLE METHOD BASED ON FILTERS

    private void updateWorkTable() throws IOException {
        Employee selectedEmployee = comboBoxFilterEmployee.getValue(); // Get the selected employee
        Project selectedProject = comboBoxFilterProject.getValue(); // Get the selected project

        try {
            WorkDao workDao = new WorkDao(new ConnectionHandler()); 

            if (selectedEmployee == null && selectedProject == null) {
                // Both "All Employees" and "All Projects" selected
                List<Work> allWork = workDao.getFilteredWork(null, null); // Get all work records
                tableViewDataShow.setItems(FXCollections.observableArrayList(allWork)); // Set the table items

                // Get the total hours worked by all consultants across all projects
                int totalHours = workDao.getTotalHoursWorked(); 
                responseLabel.setText("Total hours worked by all consultants: " + totalHours); // Set the response label

            } else if (selectedEmployee != null && selectedProject == null) {
                // Specific employee selected, "All Projects" selected
                int totalHoursByConsultant = workDao.getTotalHoursWorkedByEmployee(selectedEmployee.getEmployeeNo()); // Get total hours worked by the selected consultant

                List<Work> filteredWorkList = workDao.getFilteredWork(selectedEmployee, null); // Get filtered work records
                tableViewDataShow.setItems(FXCollections.observableArrayList(filteredWorkList)); // Set the table items

                // Display total hours worked by the selected consultant
                responseLabel.setText("Total hours worked by " + selectedEmployee.getEmpFirstName() + " " +
                        selectedEmployee.getEmpLastName() + ": " + totalHoursByConsultant); // Set the response label

            } else {
                // Other filtering scenarios
                List<Work> filteredWorkList = workDao.getFilteredWork(selectedEmployee, selectedProject); // Get filtered work records
                tableViewDataShow.setItems(FXCollections.observableArrayList(filteredWorkList)); // Set the table items

                responseLabel.setText("Filtered records displayed."); // Set the response label
            }
        } catch (DaoException e) {
            e.printStackTrace();
        }
    }

    // OPEN ADD NEW WORK VIEW

    @FXML
    private void handleButtonAddNew(ActionEvent event) {
        try {
            // Load the AddView.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddView.fxml"));
            AnchorPane addViewPane = loader.load();

            // Get the controller for the AddView
            AddViewController addViewController = loader.getController();

            // Call the showAddWork method in the AddViewController
            addViewController.showAddWork();

            // Create a new stage for the AddView
            Stage stage = new Stage();
            stage.setScene(new Scene(addViewPane));
            stage.setTitle("Add New Work");

            // Add an event handler to refresh the table after adding a new work record
            stage.setOnHidden(e -> {
                try {
                    updateWorkTable();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // OPEN EDIT WORK VIEW FROM SELECTED WORK RECORD

    @FXML
    private void handleButtonEdit(ActionEvent event) {
        Work selectedWork = tableViewDataShow.getSelectionModel().getSelectedItem();
        if (selectedWork != null) {
            openEditWorkView(selectedWork); // Open the edit view with the selected work
        } else {
            responseLabel.setText("No work record selected for editing.");
        }
    }

    // OPEN EDIT WORK VIEW METHOD

    private void openEditWorkView(Work selectedWork) {
        try {
            // Load the EditView.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditView.fxml"));
            AnchorPane editViewPane = loader.load();

            // Get the controller for the EditView
            EditViewController editViewController = loader.getController();
            editViewController.setWork(selectedWork);
            editViewController.showEditWork();

            // Create a new stage for the EditView
            Stage stage = new Stage();
            stage.setScene(new Scene(editViewPane));
            stage.setTitle("Edit Work");

            // Add an event handler to refresh the table when the window is closed
            stage.setOnHiding(e -> {
                try {
                    updateWorkTable();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            // Show the EditView stage
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while opening the edit view.");
        }
    }

    // DELETE WORK RECORD

    @FXML
    private void handleButtonDelete(ActionEvent event) {
        Work selectedWork = tableViewDataShow.getSelectionModel().getSelectedItem();

        if (selectedWork != null) {
            // Show a confirmation dialog before deleting the selected work
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Work");
            alert.setHeaderText("Are you sure you want to delete the work record?");
            alert.setContentText("This action cannot be undone.");

            // If the user confirms the deletion
            if (alert.showAndWait().get() == ButtonType.OK) {
                try {
                    WorkDao workDao = new WorkDao(new ConnectionHandler());
                    workDao.deleteWork(selectedWork.getEmployee().getEmployeeNo(),
                            selectedWork.getProject().getProjectNo());
                    updateWorkTable(); // Refresh the table after deletion
                } catch (DaoException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            // Show a warning dialog if no item is selected
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a work record to delete.");
        }
    }

    // Helper method to show alerts
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
