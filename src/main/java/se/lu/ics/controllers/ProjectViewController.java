package se.lu.ics.controllers;

import java.io.IOException;
import java.sql.Date;
import java.util.List;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import se.lu.ics.Main;
import se.lu.ics.data.ConnectionHandler;
import se.lu.ics.data.DaoException;
import se.lu.ics.data.ProjectDao;
import se.lu.ics.models.Project;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;

public class ProjectViewController {

    @FXML
    private TableView<Project> tableViewDataShow;

    @FXML
    private TextField textFieldSearch;

    @FXML
    private Button buttonEditProject;

    @FXML
    private Label responseLabel;

    @FXML
    private CheckBox checkBoxEveryConsultant;

    @FXML
    private Button buttonSearch;

    @FXML
    private TableColumn<Project, Integer> columnProjectID;

    @FXML
    private TableColumn<Project, String> columnProjectName;

    @FXML
    private TableColumn<Project, String> columnProjectDesc;

    @FXML
    private TableColumn<Project, Date> columnStartDate;

    @FXML
    private TableColumn<Project, Integer> columnMilestones;

    private ProjectDao projectDao;

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

    // Constructor to initialize the ProjectDao

    public ProjectViewController() {
        try {
            projectDao = new ProjectDao(new ConnectionHandler());
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception (e.g., show an alert or log the error)
        }
    }

    // Initialize method to set up the table columns and load all projects

    @FXML
    public void initialize() {
        // Set up the table columns
        
        columnProjectID.setCellValueFactory(new PropertyValueFactory<>("projectNo")); // Use projectNo for ID :)))

        columnProjectName.setCellValueFactory(new PropertyValueFactory<>("projectName"));

        columnProjectDesc.setCellValueFactory(new PropertyValueFactory<>("projectDescription"));

        columnStartDate.setCellValueFactory(new PropertyValueFactory<>("projectStartDate"));

        // Set up the Milestones column to show the number of milestones for each project
        columnMilestones.setCellValueFactory(cellData -> { // Use cellData to get the Project object
            Project project = cellData.getValue(); // Get the Project object from the cellData
            int milestoneCount = 0; // Initialize the milestone count to 0
            try {
                // Use projectNo to get the number of milestones
                milestoneCount = projectDao.getNumberOfMilestones(project.getProjectNo());
            } catch (DaoException e) {
                showAlert(Alert.AlertType.ERROR, "Initialization Error",
                        "Failed to initialize Project DAO: " + e.getMessage());
            }
            return new SimpleIntegerProperty(milestoneCount).asObject();
        });

        // Add columns to the table
        tableViewDataShow.getColumns()
                .setAll(List.of(columnProjectID, columnProjectName, columnProjectDesc, columnStartDate,
                        columnMilestones));

        // Load all projects by default
        loadAllProjects();

        // Listen to selection changes in the table
        tableViewDataShow.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                buttonEditProject.setDisable(false); // Enable the edit button when a project is selected
            } else {
                buttonEditProject.setDisable(true); // Disable the edit button when no project is selected
            }
        });

        // Add listener to the CheckBox
        checkBoxEveryConsultant.selectedProperty()
                .addListener((observable, oldValue, newValue) -> handleCheckBoxEveryConsultant());
    }

    // SEARCH BUTTON HANDLER

    @FXML
    private void handleButtonSearch(ActionEvent event) {
        String searchTerm = textFieldSearch.getText(); // Get the search term from the text field

        if (searchTerm == null || searchTerm.isEmpty()) { // If the search term is empty or null
            loadAllProjects(); // Load all projects
        } else {
            try {
                int projectNo = Integer.parseInt(searchTerm); // Parse the search term to an integer
                Project project = projectDao.findByProjectNo(projectNo); // Find the project by projectNo
                tableViewDataShow.setItems(FXCollections.observableArrayList(project)); // Show the project in the table
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Project ID must be a number.");
            } catch (DaoException e) {
                showAlert(Alert.AlertType.ERROR, "Project Not Found", "No project with ID " + searchTerm + " found.");
            }
        }
    }

    // LOAD ALL PROJECTS METHOD

    private void loadAllProjects() {
        try {
            List<Project> projects = projectDao.getAllProjects(); // Get all projects from the database
            ObservableList<Project> projectList = FXCollections.observableArrayList(projects); // Create an observable list
            tableViewDataShow.setItems(projectList); // Set the items in the table
        } catch (DaoException e) {
            e.printStackTrace();
            // Handle error (e.g., show an alert)
            showAlert(Alert.AlertType.ERROR, "Error Loading Projects", "Could not load projects: " + e.getMessage());
        }
    }

    // LOAD PROJECTS THAT ALL EMPLOYEES ARE ASSIGNED TO

    private void loadProjectsAllEmployeesAssigned() {
        try {
            List<Project> projects = projectDao.getProjectsAllEmployeesAssigned(); // Get projects with all employees assigned
            ObservableList<Project> projectList = FXCollections.observableArrayList(projects); // Create an observable list
            tableViewDataShow.setItems(projectList); // Set the items in the table
        } catch (DaoException e) {
            e.printStackTrace();
            // Handle error (e.g., show an alert)
            showAlert(Alert.AlertType.ERROR, "Error Loading Projects", "Could not load projects: " + e.getMessage());
        }
    }

    // CHECKBOX HANDLER TO SHOW PROJECTS THAT ALL EMPLOYEES ARE ASSIGNED TO

    @FXML
    private void handleCheckBoxEveryConsultant() {
        if (checkBoxEveryConsultant.isSelected()) { // If the CheckBox is selected
            loadProjectsAllEmployeesAssigned(); // Load projects with all employees assigned
            // Set search field to empty and disable it
            textFieldSearch.setText(""); 
            textFieldSearch.setDisable(true);
            // Disable search button
            buttonSearch.setDisable(true);
        } else {
            loadAllProjects();
            // Enable search field
            textFieldSearch.setDisable(false);
            // Enable search button
            buttonSearch.setDisable(false);
        }
    }

    // OPEN ADD VIEW METHOD

    @FXML
    private void handleButtonAddNew(ActionEvent event) {
        try {
            // Load the AddView.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddView.fxml"));
            AnchorPane addViewPane = loader.load();

            // Get the controller for the AddView
            AddViewController addViewController = loader.getController();
            addViewController.showAddProject();

            // Create a new stage for the AddView
            Stage stage = new Stage();
            stage.setScene(new Scene(addViewPane));
            stage.setTitle("Add New Project");

            // Add an event handler to refresh the table when the window is closed
            stage.setOnHiding(e -> {
                loadAllProjects(); // Refresh the project list when the window is closed
            });

            // Show the AddView stage
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while opening the add view.");
        }
    }

    // OPEN EDIT VIEW WITH SELECTED PROJECT

    @FXML
    private void handleButtonEdit(ActionEvent event) {
        Project selectedProject = tableViewDataShow.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            openEditProjectView(selectedProject); // Open the edit view with the selected project
        } else {
            responseLabel.setText("No project selected for editing.");
        }
    }

    // OPEN EDIT VIEW METHOD

    private void openEditProjectView(Project selectedProject) {
        try {
            // Load the EditView.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditView.fxml"));
            AnchorPane editViewPane = loader.load();

            // Get the controller for the EditView
            EditViewController editViewController = loader.getController();
            editViewController.setProject(selectedProject);
            editViewController.showEditProject();

            // Create a new stage for the EditView
            Stage stage = new Stage();
            stage.setScene(new Scene(editViewPane));
            stage.setTitle("Edit Project");

            // Add an event handler to refresh the table when the window is closed
            stage.setOnHiding(e -> {
                loadAllProjects(); // Refresh the project list when the window is closed
            });

            // Show the EditView stage
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while opening the edit view.");
        }
    }

    // REMOVE BUTTON HANDLER

    @FXML
    private void handleButtonRemove(ActionEvent event) {
        Project selectedProject = tableViewDataShow.getSelectionModel().getSelectedItem();

        if (selectedProject != null) {
            // Show a confirmation dialog before deleting the project
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Project");
            alert.setHeaderText(
                    "Are you sure you want to delete the project: " + selectedProject.getProjectName() + "?");
            alert.setContentText("This action cannot be undone.");

            // If the user confirms the deletion
            if (alert.showAndWait().get() == ButtonType.OK) {
                try {
                    // Use projectNo for deletion
                    projectDao.deleteProject(selectedProject.getProjectNo());
                    loadAllProjects(); // Refresh the table after deletion
                    responseLabel.setText("Project '" + selectedProject.getProjectName() + "' successfully removed!");
                } catch (DaoException e) {
                    e.printStackTrace();
                    responseLabel.setText("Error occurred while deleting the project.");
                }
            }
        } else {
            responseLabel.setText("No project selected for deletion.");
        }
    }

    // SHOW ALERT METHOD

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
