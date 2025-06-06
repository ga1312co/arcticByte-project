package se.lu.ics.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import se.lu.ics.data.ConnectionHandler;
import se.lu.ics.data.DaoException;
import se.lu.ics.data.EmployeeDao;
import se.lu.ics.data.MilestoneDao;
import se.lu.ics.data.ProjectDao;
import se.lu.ics.data.WorkDao;
import se.lu.ics.models.Project;
import se.lu.ics.models.Employee;
import se.lu.ics.models.Milestone;
import se.lu.ics.models.Work;
import java.sql.Date;
import java.util.Optional;
import java.io.IOException;

public class AddViewController {

    @FXML
    private AnchorPane anchorPaneAddProject;

    @FXML
    private AnchorPane anchorPaneAddEmployee;

    @FXML
    private AnchorPane anchorPaneAddMilestone;

    @FXML
    private AnchorPane anchorPaneAddWork;

    @FXML
    private TextField textFieldProjectName;

    @FXML
    private TextField textFieldProjectDesc;

    @FXML
    private DatePicker datePickerProject;

    @FXML
    private TextField textFieldEmployeeFirstName;

    @FXML
    private TextField textFieldEmployeeLastName;

    @FXML
    private TextField textFieldEmployeeTitle;

    @FXML
    private TextField textFieldEmployeeAddress;

    @FXML
    private ComboBox<Project> comboBoxMilestoneProject;

    @FXML
    private TextField textFieldMilestoneType;

    @FXML
    private DatePicker datePickerMilestone;

    @FXML
    private ComboBox<Employee> comboBoxRegisterWorkEmployee;

    @FXML
    private ComboBox<Project> comboBoxRegisterWorkProject;

    @FXML
    private TextField textFieldRegisterWorkAssignedHours;

    @FXML
    private TextField textFieldRegisterWorkHoursWorked;

    // Initialize DAO objects

    private EmployeeDao employeeDao;
    private ProjectDao projectDao;
    private MilestoneDao milestoneDao;
    private WorkDao workDao;

    // Constructor to initialize DAO objects

    public AddViewController() throws IOException {
        ConnectionHandler connectionHandler = new ConnectionHandler();
        this.employeeDao = new EmployeeDao(connectionHandler);
        this.projectDao = new ProjectDao(connectionHandler);
        this.milestoneDao = new MilestoneDao(connectionHandler);
        this.workDao = new WorkDao(connectionHandler);
    }

    // Populate comboboxes in their respective methods

    public void showAddProject() {
        anchorPaneAddProject.setVisible(true);
        anchorPaneAddEmployee.setVisible(false);
        anchorPaneAddMilestone.setVisible(false);
        anchorPaneAddWork.setVisible(false);
    }

    public void showAddEmployee() {
        anchorPaneAddProject.setVisible(false);
        anchorPaneAddEmployee.setVisible(true);
        anchorPaneAddMilestone.setVisible(false);
        anchorPaneAddWork.setVisible(false);
    }

    public void showAddMilestone() {
        anchorPaneAddProject.setVisible(false);
        anchorPaneAddEmployee.setVisible(false);
        anchorPaneAddMilestone.setVisible(true);
        anchorPaneAddWork.setVisible(false);

        try {
            comboBoxMilestoneProject.getItems().clear();
            comboBoxMilestoneProject.getItems().addAll(projectDao.getAllProjects());
        } catch (DaoException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load projects: " + e.getMessage());
        }
    }

    public void showAddWork() {
        anchorPaneAddProject.setVisible(false);
        anchorPaneAddEmployee.setVisible(false);
        anchorPaneAddMilestone.setVisible(false);
        anchorPaneAddWork.setVisible(true);

        try {
            comboBoxRegisterWorkEmployee.getItems().clear();
            comboBoxRegisterWorkProject.getItems().clear();

            comboBoxRegisterWorkEmployee.getItems().addAll(employeeDao.getAllEmployees());
            comboBoxRegisterWorkProject.getItems().addAll(projectDao.getAllProjects());
        } catch (DaoException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load employees or projects: " + e.getMessage());
        }
    }

    // ADD NEW PROJECT BUTTON HANDLER

    @FXML
    private void handleButtonAddNewProject() {
        try {
            // Get input values from text fields
            String projectName = textFieldProjectName.getText();
            String projectDesc = textFieldProjectDesc.getText();
            Date projectStartDate = null;
            if (datePickerProject.getValue() != null) {
                projectStartDate = Date.valueOf(datePickerProject.getValue());
            }

            // Validate inputs
            if (projectName.isEmpty() || projectDesc.isEmpty() || projectStartDate == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
                return;
            }

            // Generate the next ProjectNo
            int projectNo = projectDao.getNextProjectNo();

            // Create a new Project object
            Project project = new Project(projectNo, projectName, projectDesc, projectStartDate);

            // Register the project
            projectDao.registerProject(project);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Project added successfully!");

        } catch (DaoException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add project: " + e.getMessage());
        }
    }

    // ADD NEW EMPLOYEE BUTTON HANDLER

    @FXML
    private void handleButtonAddNewEmployee() throws IOException {
        try {
            // Get input values from text fields
            String empFirstName = textFieldEmployeeFirstName.getText();
            String empLastName = textFieldEmployeeLastName.getText();
            String empTitle = textFieldEmployeeTitle.getText();
            String empAddress = textFieldEmployeeAddress.getText();

            // Validate inputs
            if (empFirstName.isEmpty() || empLastName.isEmpty() || empTitle.isEmpty() || empAddress.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
                return;
            }

            // Generate the next EmployeeNo
            int employeeNo = employeeDao.getNextEmployeeNo();

            // Create a new Employee object
            Employee employee = new Employee(employeeNo, empFirstName, empLastName, empTitle, empAddress);

            // Register the employee
            employeeDao.registerEmployee(employee);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Employee added successfully!");

        } catch (DaoException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add employee: " + e.getMessage());
        }
    }

    // ADD NEW MILESTONE BUTTON HANDLER

    @FXML
    private void handleButtonAddNewMilestone() {
        // Get input values from text fields and combobox
        String milestoneType = textFieldMilestoneType.getText();
        Project project = comboBoxMilestoneProject.getValue();

        // Validate inputs
        if (milestoneType.isEmpty() || project == null || datePickerMilestone.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
            return;
        }

        // Get milestone date from date picker
        Date milestoneDate = Date.valueOf(datePickerMilestone.getValue());

        // If the date is before 2022-01-01, show an error message
        if (milestoneDate.before(Date.valueOf("2022-01-01"))) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a date after 2022-01-01.");
            return;
        }

        // Create a new Milestone object
        Milestone milestone = new Milestone(milestoneType, milestoneDate, project);

        try {
            // Register the milestone
            milestoneDao.registerMilestone(milestone);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Milestone added successfully!");
        } catch (DaoException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add milestone: " + e.getMessage());
        }
    }

    // ADD NEW WORK BUTTON HANDLER

    @FXML
    private void handleButtonAddNewWork() {
        // Get selected employee and project from comboboxes
        Employee employee = comboBoxRegisterWorkEmployee.getValue();
        Project project = comboBoxRegisterWorkProject.getValue();

        // Initialize variables for assigned hours and hours worked
        int assignedHours;
        int hoursWorked;

        // If any of the fields are empty, show an error message
        if (employee == null || project == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select an employee and a project.");
            return;
        }

        // Get assigned hours and hours worked from text fields
        try {
            assignedHours = Integer.parseInt(textFieldRegisterWorkAssignedHours.getText());
            hoursWorked = Integer.parseInt(textFieldRegisterWorkHoursWorked.getText());

            // If hours are negative, show an error message
            if (assignedHours < 0 || hoursWorked < 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "Hours cannot be negative.");
                return; // Exit the method if input is invalid
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter valid numbers for hours.");
            return; // Exit the method if input is invalid
        }

        // *CHECK IF TOTAL PROJECT HOURS EXCEED 60% OF COMPANY'S RESOURCES*

        try {
            // Calculate total project hours and company total hours
            int totalProjectHours = workDao.getTotalProjectHours(project.getProjectNo()) + assignedHours;
            int companyTotalHours = workDao.getTotalHoursWorked();

            // Check if total project hours exceed 60% of the company's resources
            if (companyTotalHours > 0 && totalProjectHours > companyTotalHours * 0.6) {
                // Show confirmation dialog to the user
                boolean proceed = showConfirmationDialog(
                        "Warning",
                        "Allocating this work will exceed 60% of the company's resources for this project.\n" +
                                "Do you want to proceed?");

                // If user chooses not to proceed, return early
                if (!proceed) {
                    return; // Exit the method to allow for changes
                }
            }

            // Proceed to add the work record
            Work work = new Work(employee, project, assignedHours, hoursWorked);
            workDao.registerWork(work);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Work record added successfully!");

        } catch (DaoException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add work record: " + e.getMessage());
        }
    }

    // CONFIRMATION DIALOG METHOD

    private boolean showConfirmationDialog(String title, String message) {
        // Create a custom dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        // Create a text area to hold the message
        TextArea textArea = new TextArea(message);
        textArea.setWrapText(true); // Allow text to wrap
        textArea.setEditable(false); // Make it read-only
        textArea.setPrefWidth(400); // Set preferred width
        textArea.setPrefHeight(100); // Set preferred height

        // Add the text area to the dialog
        dialog.getDialogPane().setContent(textArea);

        // Add buttons to the dialog
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        dialog.getDialogPane().getButtonTypes().setAll(yesButton, noButton);

        // Show the dialog and wait for a response
        Optional<ButtonType> result = dialog.showAndWait();
        return result.isPresent() && result.get() == yesButton; // Return true if user clicked Yes
    }

    // SHOW ALERT METHOD

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
