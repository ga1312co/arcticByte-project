package se.lu.ics.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Label;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
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
import javafx.util.Callback;

public class EmployeeViewController {

    private EmployeeDao employeeDao;
    private Label responseLabel; 

    // Constructor to initialize the EmployeeDao

    public EmployeeViewController() {
        try {
            ConnectionHandler connectionHandler = new ConnectionHandler();
            this.employeeDao = new EmployeeDao(connectionHandler);
        } catch (IOException e) {
            if (responseLabel != null) {
                responseLabel.setText("Error loading employee data: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    @FXML
    private TableView<Employee> tableViewDataShow;

    @FXML
    private TableColumn<Employee, Integer> columnEmployeeID;

    @FXML
    private TableColumn<Employee, String> columnFirstName;

    @FXML
    private TableColumn<Employee, String> columnLastName;

    @FXML
    private TableColumn<Employee, String> columnTitle;

    @FXML
    private TableColumn<Employee, String> columnAddress;

    @FXML
    private ComboBox<String> comboBoxOption;

    @FXML
    private TextField searchFieldEmployee;

    @FXML
    private Button searchButton;

    @FXML
    private ComboBox<Project> comboBoxProject;

    @FXML
    private Label responseLabelEmployee;

    @FXML
    private Button buttonEditEmployee;

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

    // Initialize method to set up the EmployeeViewController

    @FXML
    public void initialize() {
        // Set up the ComboBox with options
        comboBoxOption.getItems().clear();
        comboBoxOption.getItems().add("Employee ID");
        comboBoxOption.getItems().add("Employee Title");
        comboBoxOption.getItems().add("Lazy employees (< 4 projects)");

        // Add listener to the ComboBoxOption
        comboBoxOption.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            handleComboBoxSelection(newValue);
        });

        // Update the cell value factory to use employeeNo instead of employeeID
        columnEmployeeID.setCellValueFactory(new PropertyValueFactory<>("employeeNo"));
        columnFirstName.setCellValueFactory(new PropertyValueFactory<>("empFirstName"));
        columnLastName.setCellValueFactory(new PropertyValueFactory<>("empLastName"));
        columnTitle.setCellValueFactory(new PropertyValueFactory<>("empTitle"));
        columnAddress.setCellValueFactory(new PropertyValueFactory<>("empAddress"));
        tableViewDataShow.setPlaceholder(new Label("No data for selected option found"));

        try {
            loadComboBoxWithProjects(); // Load the ComboBox with projects
            loadEmployeeData(); // Load the employee data into the table
            loadAllEmployeesResponseLabel(); // Load the total number of employees into the response label
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Add listener to the ComboBoxProject
        comboBoxProject.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                loadEmployeesByProject(newValue);
            } else {
                loadEmployeeData(); // Show all employees if "Show All Projects" is selected
            }
        });

        // setOnShowing event for the ComboBoxProject to load the projects when the ComboBox is clicked
        comboBoxProject.setOnShowing(event -> {
            try {
                loadComboBoxWithProjects();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    // ADD NEW EMPLOYEE BUTTON HANDLER
    // Opens the AddView to add a new employee

    @FXML
    private void handleButtonAddNew(ActionEvent event) {
        try {
            // Load the AddView.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddView.fxml"));
            AnchorPane addViewPane = loader.load();

            // Get the controller for the AddView
            AddViewController addViewController = loader.getController();

            addViewController.showAddEmployee();

            // Create a new stage for the AddView
            Stage stage = new Stage();
            stage.setScene(new Scene(addViewPane));
            stage.setTitle("Add New Employee");
            // Add an event handler to refresh the table after adding a new employee
            stage.setOnHidden(e -> {
                loadEmployeeData();
            });
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // EDIT EMPLOYEE BUTTON HANDLER
    // Opens the EditView with the selected employee

    @FXML
    private void handleButtonEdit(ActionEvent event) {
        Employee selectedEmployee = tableViewDataShow.getSelectionModel().getSelectedItem();
        if (selectedEmployee != null) {
            openEditEmployeeView(selectedEmployee); // Open the edit view with the selected employee
        } else {
            responseLabelEmployee.setText("No employee selected for editing.");
        }
    }

    // openEditEmployeeView method opens EditView.fxml and sets the selected
    // employee
    // to the controller
    private void openEditEmployeeView(Employee selectedEmployee) {
        try {
            // Load the EditView.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditView.fxml"));
            AnchorPane editViewPane = loader.load();

            // Get the controller for the EditView
            EditViewController editViewController = loader.getController();
            editViewController.setEmployee(selectedEmployee); // Set the selected employee
            editViewController.showEditEmployee(); // Show the selected employee in the EditView

            // Create a new stage for the EditView
            Stage stage = new Stage();
            stage.setScene(new Scene(editViewPane));
            stage.setTitle("Edit Employee");

            // Add an event handler to refresh the table when the window is closed
            stage.setOnHiding(e -> {
                // Refresh the employee list when the window is closed
                try {
                    loadEmployeeData();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            // Show the EditView stage
            stage.show();

        } catch (IOException e) {
            showAlert("Error", "An error occurred while opening the edit view. Please try again.", AlertType.ERROR);
        }
    }

    // SEARCH BUTTON HANDLER
    // Divides the search into three methods: searchByEmployeeNo, searchByTitle, and searchLazyEmployees

    @FXML
    public void handleSearchButton(ActionEvent event) {
        // Get the selected option from the ComboBox
        String selectedOption = comboBoxOption.getSelectionModel().getSelectedItem();
        if (selectedOption != null) {
            if (selectedOption.equals("Employee ID")) {
                searchByEmployeeNo(); 
            } else if (selectedOption.equals("Employee Title")) {
                searchByTitle();
            } else if (selectedOption.equals("Lazy employees (< 4 projects)")) {
                searchLazyEmployees();
            }
        }
    }

    // SEARCH BY EMPLOYEE NO

    private void searchByEmployeeNo() {
        try {
            int employeeNo = Integer.parseInt(searchFieldEmployee.getText()); // Parse the employeeNo to an integer
            Employee employee = employeeDao.findByEmployeeNo(employeeNo); // Find the employee by employeeNo
            if (employee != null) {
                tableViewDataShow.getItems().setAll(employee); // Set the table to show the employee
                searchFieldEmployee.clear(); // Clear the search field
            } else {
                showAlert(
                        "Employee not found",
                        "Employee with Employee ID " + employeeNo + " not found",
                        Alert.AlertType.INFORMATION);
            }
        } catch (NumberFormatException e) {
            showAlert(
                    "Invalid Employee ID",
                    "Employee ID must be a number",
                    Alert.AlertType.ERROR);
            searchFieldEmployee.clear();
        } catch (DaoException e) {
            showAlert(
                    "Error Searching for Employee",
                    "Could not search for employee",
                    Alert.AlertType.ERROR);
        }
    }

    // SEARCH BY TITLE

    private void searchByTitle() {
        try {
            List<Employee> employees = employeeDao.findByTitle(searchFieldEmployee.getText()); // Find employees by title
            if (!employees.isEmpty()) {
                tableViewDataShow.getItems().setAll(employees); // Set the table to show the employees
            } else {
                showAlert(
                        "Employee not found",
                        "No employees with the title " + searchFieldEmployee.getText() + " found.",
                        Alert.AlertType.INFORMATION);
                searchFieldEmployee.clear();
            }
        } catch (DaoException e) {
            showAlert(
                    "Error Searching for Employee",
                    "Could not search for employee",
                    Alert.AlertType.ERROR);
        }
    }

    // SEARCH LAZY EMPLOYEES

    private void searchLazyEmployees() {
        try {
            List<Employee> allEmployees = employeeDao.getAllEmployees(); // Get all employees
            List<Employee> lazyEmployees = new ArrayList<>(); // Create a list for lazy employees
            WorkDao workDao = new WorkDao(new ConnectionHandler()); // Create a WorkDao to count projects

            for (Employee emp : allEmployees) { // Loop through all employees
                int projectCount = workDao.countProjectsForEmployee(emp.getEmployeeNo()); // Count projects for employee
                if (projectCount <= 3) { // If the employee has 3 or fewer projects
                    lazyEmployees.add(emp); // Add the employee to the lazy employees list
                }
            }

            if (!lazyEmployees.isEmpty()) { // If there are lazy employees
                tableViewDataShow.getItems().setAll(lazyEmployees); // Set the table to show the lazy employees
            } else {
                showAlert("No Lazy Employees", "No employees working on 3 or fewer projects found",
                        Alert.AlertType.INFORMATION);
            }
        } catch (DaoException | IOException e) {
            showAlert("Error Loading Employees", "Could not load the lazy employees", Alert.AlertType.ERROR);
        }
    }

    // REMOVE EMPLOYEE BUTTON HANDLER

    @FXML
    private void handleButtonRemove(ActionEvent event) {
        Employee selectedEmployee = tableViewDataShow.getSelectionModel().getSelectedItem(); // Get the selected employee

        if (selectedEmployee != null) {
            // Show a confirmation dialog before deleting the employee
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Employee");
            alert.setHeaderText("Are you sure you want to delete the employee: " + selectedEmployee.getEmpFirstName()
                    + " " + selectedEmployee.getEmpLastName() + "?");
            alert.setContentText("This action cannot be undone.");

            // If the user confirms the deletion
            if (alert.showAndWait().get() == ButtonType.OK) {
                try {
                    // Use employeeNo for deletion
                    employeeDao.deleteEmployee(selectedEmployee.getEmployeeNo());
                    loadEmployeeData(); // Refresh the table after deletion

                    showAlert(
                            "Employee deleted",
                            "Employee '" + selectedEmployee.getEmpFirstName() + " "
                                    + selectedEmployee.getEmpLastName() + "' has been removed from the database.",
                            Alert.AlertType.INFORMATION);
                } catch (DaoException e) {
                    e.printStackTrace();
                    showAlert("Error deleting employee",
                            "Could not delete employee, if the problem persists, please contact support",
                            Alert.AlertType.ERROR);
                }
            }
        } else {
            showAlert("Error", "No employee selected for deletion, please select an employee to delete",
                    AlertType.INFORMATION);
        }
    }

    // LOAD EMPLOYEE DATA

    private void loadEmployeeData() {
        ObservableList<Employee> employeeList = FXCollections.observableArrayList(); // Create an observable list for employees

        try {
            List<Employee> employees = employeeDao.getAllEmployees(); // Get all employees
            employeeList.addAll(employees); // Add all employees to the list
        } catch (DaoException e) {
            showAlert(
                    "Error Loading Employees",
                    "Could not load employee data. If the problem persists, please contact support",
                    AlertType.ERROR);
            e.printStackTrace();
        }

        tableViewDataShow.setItems(employeeList); // Set the table to show the employees
    }

    // HANDLE COMBOBOX SELECTION

    private void handleComboBoxSelection(String selection) {
        if (selection.equals("Employee ID") || selection.equals("Employee Title")) { // If the selection is by employee ID or title
            searchFieldEmployee.setDisable(false); // Make the search field visible
            searchButton.setDisable(false); // Enable the search button
            // reset table to show all employees
            loadEmployeeData();
            searchFieldEmployee
                    .setPromptText(selection.equals("Employee ID") ? "Enter Employee ID" : "Enter Employee Title"); 
        } else if (selection.equals("Lazy employees (< 4 projects)")) {
            searchFieldEmployee.setDisable(true);
            ; // Hide the search field for lazy workers
            searchButton.setDisable(true); // Enable the search button
            searchFieldEmployee.clear();
            searchLazyEmployees(); // Search for lazy employees
        }
    }

    // LOAD COMBOBOX WITH PROJECTS

    private void loadComboBoxWithProjects() throws IOException {
        try {
            ProjectDao projectDao = new ProjectDao(new ConnectionHandler());
            List<Project> projects = projectDao.getAllProjects();

            // Clear existing items
            comboBoxProject.getItems().clear();

            // Add "Show All Projects" option (represented by null)
            comboBoxProject.getItems().add(null);

            // Add projects
            comboBoxProject.getItems().addAll(projects);

            // Set cell factory to display project names
            comboBoxProject.setCellFactory(new Callback<ListView<Project>, ListCell<Project>>() {
                @Override
                public ListCell<Project> call(ListView<Project> param) {
                    return new ListCell<Project>() {
                        @Override
                        protected void updateItem(Project item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText("All Projects");
                            } else {
                                setText(item.getProjectName());
                            }
                        }
                    };
                }
            });

            // Also set the button cell
            comboBoxProject.setButtonCell(new ListCell<Project>() {
                @Override
                protected void updateItem(Project item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Sort By Projects");
                    } else {
                        setText(item.getProjectName());
                    }
                }
            });

            // Select the current project if one is selected
            Project selectedProject = comboBoxProject.getSelectionModel().getSelectedItem();
            if (selectedProject != null) {
                for (Project project : comboBoxProject.getItems()) {
                    if (project != null && project.getProjectNo() == selectedProject.getProjectNo()) {
                        comboBoxProject.getSelectionModel().select(project);
                        break;
                    }
                }
            } else {
                // Select "Show All Projects" if no project is selected
                comboBoxProject.getSelectionModel().selectFirst();
            }

        } catch (DaoException e) {
            showAlert(
                    "Error Loading Projects",
                    "Could not load projects. If the problem persists, please contact support",
                    AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // LOAD EMPLOYEES BY PROJECT

    private void loadEmployeesByProject(Project project) {
        ObservableList<Employee> employeeList = FXCollections.observableArrayList(); // Create an observable list for employees
        try {
            List<Employee> employees = employeeDao.findEmployeesByProject(project.getProjectNo()); // Find employees by project
            employeeList.addAll(employees); // Add employees to the list
            tableViewDataShow.setItems(employeeList); // Set the table to show the employees

        } catch (DaoException e) {
            showAlert("Error Loading Employees",
                    "Could not load employees for the selected project. If the problem persists, please contact support",
                    AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // LOAD ALL EMPLOYEES RESPONSE LABEL

    private void loadAllEmployeesResponseLabel() {
        try {
            List<Employee> employees = employeeDao.getAllEmployees(); // Get all employees
            responseLabelEmployee.setText("Total number of employees: " + employees.size()); // Set the response label
        } catch (DaoException e) {
            responseLabelEmployee.setText("Error loading employee count: " + e.getMessage());
            e.printStackTrace(); // Optional: Print stack trace for debugging
        }

    }

    // SHOW ALERT METHOD

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
