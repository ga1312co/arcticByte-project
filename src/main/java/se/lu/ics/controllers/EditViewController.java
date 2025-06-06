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

public class EditViewController {

    @FXML
    private AnchorPane anchorPaneEditProject;

    @FXML
    private AnchorPane anchorPaneEditEmployee;

    @FXML
    private AnchorPane anchorPaneEditMilestone;

    @FXML
    private AnchorPane anchorPaneEditWork;

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
    private ComboBox<Employee> comboBoxEditWorkEmployee;
    @FXML
    private ComboBox<Project> comboBoxEditWorkProject;
    @FXML
    private TextField textFieldEditWorkAssignedHours;
    @FXML
    private TextField textFieldEditWorkHoursWorked;

    private EmployeeDao employeeDao;
    private ProjectDao projectDao;
    private MilestoneDao milestoneDao;
    private WorkDao workDao;
    private Project project;
    private Employee employee;
    private Milestone milestone;
    private Work work;

    // Constructor with ConnectionHandler

    public EditViewController() throws IOException {
        ConnectionHandler connectionHandler = new ConnectionHandler();
        this.employeeDao = new EmployeeDao(connectionHandler);
        this.projectDao = new ProjectDao(connectionHandler);
        this.milestoneDao = new MilestoneDao(connectionHandler);
        this.workDao = new WorkDao(connectionHandler);
    }

    // Set project method
    public void setProject(Project project) {
        this.project = project;
        textFieldProjectName.setText(project.getProjectName());
        textFieldProjectDesc.setText(project.getProjectDescription());
        datePickerProject.setValue(project.getProjectStartDate().toLocalDate());
    }

    // Set employee method
    public void setEmployee(Employee employee) {
        this.employee = employee;
        textFieldEmployeeFirstName.setText(employee.getEmpFirstName());
        textFieldEmployeeLastName.setText(employee.getEmpLastName());
        textFieldEmployeeTitle.setText(employee.getEmpTitle());
        textFieldEmployeeAddress.setText(employee.getEmpAddress());
    }

    // Set milestone method
    public void setMilestone(Milestone milestone) {
        this.milestone = milestone;
        textFieldMilestoneType.setText(milestone.getMilestoneType());
        datePickerMilestone.setValue(milestone.getMilestoneDate().toLocalDate());
    }

    // Set work method
    public void setWork(Work work) {
        this.work = work;
        comboBoxEditWorkEmployee.setValue(work.getEmployee());
        comboBoxEditWorkProject.setValue(work.getProject());
        textFieldEditWorkAssignedHours.setText(String.valueOf(work.getAssignedHours()));
        textFieldEditWorkHoursWorked.setText(String.valueOf(work.getHoursWorked()));
    }

    public void showEditProject() {
        anchorPaneEditProject.setVisible(true);
        anchorPaneEditEmployee.setVisible(false);
        anchorPaneEditMilestone.setVisible(false);
        anchorPaneEditWork.setVisible(false);

    }

    public void showEditEmployee() {
        anchorPaneEditProject.setVisible(false);
        anchorPaneEditEmployee.setVisible(true);
        anchorPaneEditMilestone.setVisible(false);
        anchorPaneEditWork.setVisible(false);
    }

    public void showEditMilestone() {
        anchorPaneEditProject.setVisible(false);
        anchorPaneEditEmployee.setVisible(false);
        anchorPaneEditMilestone.setVisible(true);
        anchorPaneEditWork.setVisible(false);

        try {
            comboBoxMilestoneProject.getItems().clear();
            comboBoxMilestoneProject.getItems().addAll(projectDao.getAllProjects());
            // Select the current project for the milestone
            if (milestone != null && milestone.getProject() != null) {
                comboBoxMilestoneProject.setValue(milestone.getProject());
            }
        } catch (DaoException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load projects: " + e.getMessage());
        }
    }

    public void showEditWork() {
        anchorPaneEditProject.setVisible(false);
        anchorPaneEditEmployee.setVisible(false);
        anchorPaneEditMilestone.setVisible(false);
        anchorPaneEditWork.setVisible(true);

        try {
            comboBoxEditWorkEmployee.getItems().clear();
            comboBoxEditWorkProject.getItems().clear();

            comboBoxEditWorkEmployee.getItems().addAll(employeeDao.getAllEmployees());
            comboBoxEditWorkProject.getItems().addAll(projectDao.getAllProjects());
        } catch (DaoException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load employees or projects: " + e.getMessage());
        }
    }

    // EDIT PROJECT BUTTON HANDLER

    @FXML
    private void handleButtonEditProject() {
        // Validate that all fields are filled
        if (textFieldProjectName.getText().isEmpty() ||
                textFieldProjectDesc.getText().isEmpty() ||
                datePickerProject.getValue() == null) {

            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all fields.");
            return;
        }

        // Get values from textfields
        String projectName = textFieldProjectName.getText();
        String projectDesc = textFieldProjectDesc.getText();
        Date projectDate = Date.valueOf(datePickerProject.getValue());

        project.setProjectName(projectName);
        project.setProjectDescription(projectDesc);
        project.setProjectStartDate(projectDate);

        try {
            projectDao.updateProject(project);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Project updated successfully!");
        } catch (DaoException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update project: " + e.getMessage());
        }
    }

    // EDIT EMPLOYEE BUTTON HANDLER

    @FXML
    private void handleButtonEditEmployee() {
        // Validate that all fields are filled
        if (textFieldEmployeeFirstName.getText().isEmpty() ||
                textFieldEmployeeLastName.getText().isEmpty() ||
                textFieldEmployeeTitle.getText().isEmpty() ||
                textFieldEmployeeAddress.getText().isEmpty()) {

            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all fields.");
            return;
        }

        // Get values from textfields
        String empFirstName = textFieldEmployeeFirstName.getText();
        String empLastName = textFieldEmployeeLastName.getText();
        String empTitle = textFieldEmployeeTitle.getText();
        String empAddress = textFieldEmployeeAddress.getText();

        employee.setEmpFirstName(empFirstName);
        employee.setEmpLastName(empLastName);
        employee.setEmpTitle(empTitle);
        employee.setEmpAddress(empAddress);

        try {
            employeeDao.updateEmployee(employee);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Employee updated successfully!");
        } catch (DaoException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update employee: " + e.getMessage());
        }

    }

    // EDIT MILESTONE BUTTON HANDLER

    @FXML
    private void handleButtonEditMilestone() {
        // Validate that all fields are filled
        if (textFieldMilestoneType.getText().isEmpty() ||
                datePickerMilestone.getValue() == null ||
                comboBoxMilestoneProject.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all fields.");
            return;
        }

        // Get values from textfield, datepicker and combobox
        String milestoneType = textFieldMilestoneType.getText();
        Date milestoneDate = Date.valueOf(datePickerMilestone.getValue());
        Project project = comboBoxMilestoneProject.getValue();

        // If the date is before 2022-01-01, show an error message
        if (milestoneDate.before(Date.valueOf("2022-01-01"))) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a date after 2022-01-01.");
            return;
        }

        // Set values to the milestone object
        milestone.setMilestoneType(milestoneType);
        milestone.setMilestoneDate(milestoneDate);
        milestone.setProject(project);

        try {
            milestoneDao.updateMilestone(milestone);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Milestone updated successfully!");
        } catch (DaoException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update milestone: " + e.getMessage());
        }
    }

    // EDIT WORK BUTTON HANDLER

    @FXML
    private void handleButtonEditWork() {
        // Validate that all fields are filled
        if (comboBoxEditWorkEmployee.getValue() == null ||
                comboBoxEditWorkProject.getValue() == null ||
                textFieldEditWorkAssignedHours.getText().isEmpty() ||
                textFieldEditWorkHoursWorked.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all fields.");
            return;
        }

        // Get values from textfields and comboboxes
        Employee employee = comboBoxEditWorkEmployee.getValue();
        Project project = comboBoxEditWorkProject.getValue();
        int assignedHours;
        int hoursWorked;

        try {
            assignedHours = Integer.parseInt(textFieldEditWorkAssignedHours.getText());
            hoursWorked = Integer.parseInt(textFieldEditWorkHoursWorked.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Assigned Hours and Hours Worked must be numbers.");
            return;
        }

        // If hours are negative, show an error message
        if (assignedHours < 0 || hoursWorked < 0) {
            showAlert(Alert.AlertType.ERROR, "Error", "Hours cannot be negative.");
            return; // Exit the method if input is invalid
        }

        try {
            // Update totalProjectHours calculation if necessary
            int totalProjectHours = workDao.getTotalProjectHours(project.getProjectNo()) + assignedHours;
            int totalCompanyHours = workDao.getTotalHoursWorked(); // Assuming this method exists

            if (totalProjectHours > totalCompanyHours * 0.6) {

                boolean proceed = showConfirmationDialog(
                        "Warning",
                        "Allocating this work will exceed 60% of the company's resources for this project.\n" +
                                "Do you want to proceed?");

                if (!proceed) {
                    return;

                }
            }

            // Set values to the work object
            work.setEmployee(employee);
            work.setProject(project);
            work.setAssignedHours(assignedHours);
            work.setHoursWorked(hoursWorked);

            workDao.updateWork(work);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Work updated successfully!");
        } catch (DaoException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update work: " + e.getMessage());
        }

    }

    // SHOW ALERT METHOD

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // SHOW CONFIRMATION DIALOG METHOD

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
        textArea.setPrefHeight(100); // Set preferred height (optional)

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
}
