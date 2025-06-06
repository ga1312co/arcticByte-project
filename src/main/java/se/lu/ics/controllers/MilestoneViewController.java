package se.lu.ics.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import se.lu.ics.data.MilestoneDao;
import se.lu.ics.data.ProjectDao;
import se.lu.ics.models.Milestone;
import se.lu.ics.models.Project;
import se.lu.ics.Main;
import se.lu.ics.data.ConnectionHandler;
import se.lu.ics.data.DaoException;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class MilestoneViewController {

    @FXML
    private ComboBox<String> comboBoxProjects;

    @FXML
    private Label responseLabel;

    @FXML
    private TableView<Milestone> tableViewMilestones;

    @FXML
    private TableColumn<Milestone, String> columnMilestoneType;

    @FXML
    private TableColumn<Milestone, String> columnMilestoneDate;

    @FXML
    private TableColumn<Milestone, String> columnProjectName;

    @FXML
    private ObservableList<Milestone> milestoneList;

    @FXML
    private Button buttonRemoveMilestone;

    @FXML
    private Button buttonEditMilestone;

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
    public void initialize() throws IOException {
        columnMilestoneType.setCellValueFactory(new PropertyValueFactory<>("milestoneType"));
        columnMilestoneDate.setCellValueFactory(new PropertyValueFactory<>("milestoneDate"));

        // Bind the project name column to the project field of each milestone
        columnProjectName.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getProject().getProjectName()));

        try {
            populateProjectComboBox(); // Populate the project combobox
            comboBoxProjects.getSelectionModel().select("Show All Milestones"); // Select "Show All Milestones" by default
            loadAllMilestones(); // Load all milestones on startup
        } catch (DaoException e) {
            e.printStackTrace(); // Handle DaoException for initial population
        }
    }

    // POPULATE PROJECT COMBOBOX METHOD

    private void populateProjectComboBox() throws DaoException, IOException {
        ProjectDao projectDao = new ProjectDao(new ConnectionHandler()); // Create a new ProjectDao instance
        List<Project> projects = projectDao.getAllProjects(); // Fetch all projects from the database

        // Add "Show All Milestones" option to the combobox
        comboBoxProjects.getItems().add("Show All Milestones");

        for (Project project : projects) { // Loop through all projects
            comboBoxProjects.getItems().add(project.getProjectName()); // Add each project to the combobox
        }

        // Listen to changes in ComboBox selection and load milestones for the selected project
        comboBoxProjects.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) { // Check if the selected item is not null
                try {
                    if (newVal.equals("Show All Milestones")) { // Check if "Show All Milestones" is selected
                        loadAllMilestones(); // Load all milestones if "Show All Milestones" is selected
                    } else {
                        loadMilestonesForProject(newVal); // Load milestones for the selected project
                    }
                } catch (DaoException e) {
                    e.printStackTrace(); // Handle DaoException
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // LOAD MILESTONES FOR SPECIFIC PROJECT METHOD

    private void loadMilestonesForProject(String projectName) throws DaoException, IOException { 
        ProjectDao projectDao = new ProjectDao(new ConnectionHandler()); // Create a new ProjectDao instance

        Project selectedProject = null; // This will hold the project the user selected

        // Fetch all projects and find the one that matches the projectName
        List<Project> projects = projectDao.getAllProjects(); // Fetch all projects from the database
        for (Project project : projects) { // Loop through all projects
            if (project.getProjectName().equals(projectName)) { // Check if the project name matches the selected project
                selectedProject = project; // Set the selected project
                break; // Stop the loop once we find the project
            }
        }

        // If no project was found, throw an exception
        if (selectedProject == null) {
            throw new DaoException("Project not found: " + projectName);
        }

        // Now fetch the milestones for the selected project
        MilestoneDao milestoneDao = new MilestoneDao(new ConnectionHandler()); 
        List<Milestone> milestones = milestoneDao.getMilestonesForProject(selectedProject); 

        // Sort milestones by date
        milestones.sort(Comparator.comparing(Milestone::getMilestoneDate));

        // Convert the list of milestones to an ObservableList and update the TableView
        milestoneList = FXCollections.observableArrayList(milestones);
        tableViewMilestones.setItems(milestoneList);

        // Update the label with the number of milestones for the selected project
        responseLabel.setText("There are " + milestones.size() + " milestones in the project " + projectName);
    }

    // LOAD ALL MILESTONES METHOD

    private void loadAllMilestones() throws DaoException, IOException {
        MilestoneDao milestoneDao = new MilestoneDao(new ConnectionHandler());
        List<Milestone> allMilestones = milestoneDao.findAll();

        // Sort milestones by date
        allMilestones.sort(Comparator.comparing(Milestone::getMilestoneDate));

        // Convert the list of all milestones to an ObservableList and update the
        // TableView
        milestoneList = FXCollections.observableArrayList(allMilestones);
        tableViewMilestones.setItems(milestoneList);

        // Update the label with the total number of milestones
        responseLabel.setText("There are " + allMilestones.size() + " milestones in total across all projects.");
    }

    // OPEBN ADD NEW MILESTONE VIEW METHOD

    @FXML
    private void handleButtonAddNew(ActionEvent event) {
        try {
            // Load the AddView.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddView.fxml"));
            AnchorPane addViewPane = loader.load();

            // Get the controller for AddView
            AddViewController addViewController = loader.getController();
            // Explicitly show the Add Milestone pane
            addViewController.showAddMilestone();

            // Create a new stage for the AddView
            Stage stage = new Stage();
            stage.setScene(new Scene(addViewPane));
            stage.setTitle("Add New Milestone");

            // Add an event handler to refresh the table when the window is closed
            stage.setOnHiding(e -> {
                try {
                    loadAllMilestones(); // Refresh the milestone list when the window is closed
                } catch (DaoException | IOException ex) {
                    ex.printStackTrace();
                }
            });

            // Show the AddView stage
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // METHOD TO HANDLE DELETE MILESTONE BUTTON

    @FXML
    private void handleButtonDeleteMilestone(ActionEvent event) throws IOException {
        Milestone selectedMilestone = tableViewMilestones.getSelectionModel().getSelectedItem();

        if (selectedMilestone != null) {
            // Show a confirmation dialog before deleting the milestone
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Milestone");
            alert.setHeaderText(
                    "Are you sure you want to delete the milestone: " + selectedMilestone.getMilestoneType() + "?");
            alert.setContentText("This action cannot be undone.");

            // If the user confirms the deletion
            if (alert.showAndWait().get() == ButtonType.OK) {
                try {
                    MilestoneDao milestoneDao = new MilestoneDao(new ConnectionHandler());
                    milestoneDao.deleteMilestone(selectedMilestone.getMilestoneID());
                    loadAllMilestones(); // Refresh the milestone table
                    responseLabel
                            .setText("Milestone '" + selectedMilestone.getMilestoneType() + "' successfully removed!");
                } catch (DaoException e) {
                    e.printStackTrace();
                    responseLabel.setText("Error occurred while deleting the milestone.");
                }
            }
        } else {
            responseLabel.setText("No milestone selected for deletion.");
        }
    }

    // Method to make sure the user has selected a milestone before editing
    @FXML
    private void handleButtonEdit(ActionEvent event) {
        Milestone selectedMilestone = tableViewMilestones.getSelectionModel().getSelectedItem();
        if (selectedMilestone != null) {
            openEditMilestoneView(selectedMilestone); // Open the edit view with the selected employee
        } else {
            responseLabel.setText("No milestone selected for editing.");
        }
    }

    // OPEN EDIT MILESTONE VIEW METHOD

    private void openEditMilestoneView(Milestone selectedMilestone) {
        try {
            // Load the EditView.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditView.fxml"));
            AnchorPane editViewPane = loader.load();

            // Get the controller for the EditView
            EditViewController editViewController = loader.getController();
            editViewController.setMilestone(selectedMilestone);
            editViewController.showEditMilestone();

            // Create a new stage for the EditView
            Stage stage = new Stage();
            stage.setScene(new Scene(editViewPane));
            stage.setTitle("Edit Project");

            // Add an event handler to refresh the table when the window is closed
            stage.setOnHiding(e -> {
                try {
                    loadAllMilestones(); // Refresh the project list when the window is closed
                } catch (DaoException | IOException ex) {
                    ex.printStackTrace();
                }
            });

            // Show the EditView stage
            stage.show();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Error");
            alert.setHeaderText("An error occurred while opening the edit view.");
            alert.setContentText("Please try again.");

        }
    }
}
