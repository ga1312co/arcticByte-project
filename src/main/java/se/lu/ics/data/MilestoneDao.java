package se.lu.ics.data;

import se.lu.ics.models.Milestone;
import se.lu.ics.models.Project;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MilestoneDao {

    private ConnectionHandler connectionHandler;

    public MilestoneDao(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    // METHOD FOR REGISTER MILESTONE
    public void registerMilestone(Milestone milestone) throws DaoException {
        String query = "INSERT INTO Milestone (MilestoneType, MilestoneDate, ProjectID) VALUES (?, ?, ?)";
        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, milestone.getMilestoneType()); // Set MilestoneType
            statement.setDate(2, milestone.getMilestoneDate()); // Set MilestoneDate

            // Get ProjectID from ProjectNo
            int projectID = getProjectIDByProjectNo(milestone.getProject().getProjectNo(), connection);
            if (projectID == -1) {
                throw new DaoException("Project with ProjectNo " + milestone.getProject().getProjectNo() + " not found.");
            }

            statement.setInt(3, projectID); // Set ProjectID

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error registering milestone", e);
        }
    }

    // ADD MILESTONE TO PROJECT
    public void addMilestoneToProject(Milestone milestone, Project project) throws DaoException {
        String query = "INSERT INTO Milestone (MilestoneType, MilestoneDate, ProjectID) VALUES (?, ?, ?)";
        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, milestone.getMilestoneType());
            statement.setDate(2, milestone.getMilestoneDate());

            // Get ProjectID from ProjectNo
            int projectID = getProjectIDByProjectNo(project.getProjectNo(), connection);
            if (projectID == -1) {
                throw new DaoException("Project with ProjectNo " + project.getProjectNo() + " not found.");
            }

            statement.setInt(3, projectID);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error adding milestone to project", e);
        }
    }

    // RETRIEVE ALL MILESTONES FOR A SPECIFIC PROJECT, SORTED BY DATE
    public List<Milestone> getMilestonesForProject(Project project) throws DaoException {
        List<Milestone> milestones = new ArrayList<>(); // Create a list to store milestones
        String query = "SELECT MilestoneID, MilestoneType, MilestoneDate FROM Milestone WHERE ProjectID = ? ORDER BY MilestoneDate";
        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Get ProjectID from ProjectNo
            int projectID = getProjectIDByProjectNo(project.getProjectNo(), connection);
            if (projectID == -1) {
                throw new DaoException("Project with ProjectNo " + project.getProjectNo() + " not found.");
            }

            statement.setInt(1, projectID);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) { // Loop through the result set
                    int milestoneID = resultSet.getInt("MilestoneID");
                    String milestoneType = resultSet.getString("MilestoneType");
                    Date milestoneDate = resultSet.getDate("MilestoneDate");

                    milestones.add(new Milestone(milestoneID, milestoneType, milestoneDate, project));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Error retrieving milestones for project", e);
        }
        return milestones;
    }

    // DELETE MILESTONE BY MILESTONEID
    public void deleteMilestone(int milestoneID) throws DaoException {
        String query = "DELETE FROM Milestone WHERE MilestoneID = ?";
        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
    
            statement.setInt(1, milestoneID);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error deleting milestone", e);
        }
    }

    // UPDATE MILESTONE 
    public void updateMilestone(Milestone milestone) throws DaoException {
        String query = "UPDATE Milestone SET MilestoneType = ?, MilestoneDate = ? WHERE MilestoneID = ?";
        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, milestone.getMilestoneType());
            statement.setDate(2, milestone.getMilestoneDate());
            statement.setInt(3, milestone.getMilestoneID());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error updating milestone", e);
        }
    }

    // RETRIEVE ALL MILESTONES
    public List<Milestone> findAll() throws DaoException {
        List<Milestone> milestones = new ArrayList<>();
        String query = "SELECT MilestoneID, MilestoneType, MilestoneDate, ProjectID FROM Milestone";
        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int milestoneID = resultSet.getInt("MilestoneID");
                String milestoneType = resultSet.getString("MilestoneType");
                Date milestoneDate = resultSet.getDate("MilestoneDate");
                int projectID = resultSet.getInt("ProjectID");

                // Retrieve Project by ProjectID
                Project project = getProjectByProjectID(projectID, connection);

                milestones.add(new Milestone(milestoneID, milestoneType, milestoneDate, project));
            }
        } catch (SQLException e) {
            throw new DaoException("Error retrieving all milestones", e);
        }
        return milestones;
    }

    // HELPER METHOD TO GET PROJECTID BY PROJECTNO
    private int getProjectIDByProjectNo(int projectNo, Connection connection) throws SQLException {
        String query = "SELECT ProjectID FROM Project WHERE ProjectNo = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, projectNo);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("ProjectID");
                } else {
                    return -1; // Project not found
                }
            }
        }
    }

    // HELPER METHOD TO GET PROJECT BY PROJECTID
    private Project getProjectByProjectID(int projectID, Connection connection) throws SQLException, DaoException {
        String query = "SELECT ProjectID, ProjectNo, ProjectName, ProjectDesc, StartDate FROM Project WHERE ProjectID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, projectID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int projectNo = resultSet.getInt("ProjectNo");
                    String projectName = resultSet.getString("ProjectName");
                    String projectDesc = resultSet.getString("ProjectDesc");
                    Date startDate = resultSet.getDate("StartDate");
                    return new Project(projectID, projectNo, projectName, projectDesc, startDate);
                } else {
                    throw new DaoException("Project with ProjectID " + projectID + " not found.");
                }
            }
        }
    }
}
