package se.lu.ics.data;

import se.lu.ics.models.Project;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDao {

    private ConnectionHandler connectionHandler;

    public ProjectDao(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    // REGISTER PROJECT
    public void registerProject(Project project) throws DaoException {
        String query = "INSERT INTO Project (ProjectNo, ProjectName, ProjectDesc, StartDate) VALUES (?, ?, ?, ?)";

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            if (project == null) {
                throw new DaoException("Project cannot be null");
            }

            statement.setInt(1, project.getProjectNo());
            statement.setString(2, project.getProjectName());
            statement.setString(3, project.getProjectDescription());
            statement.setDate(4, project.getProjectStartDate());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DaoException("Error registering project", e);
        } catch (NullPointerException e) {
            throw new DaoException("Null value encountered", e);
        }
    }

    // RETRIEVE INFORMATION ABOUT A PROJECT BY ProjectNo
    public Project findByProjectNo(int projectNo) throws DaoException {
        Project project = null; // Initialize to null
        String query = "SELECT ProjectID, ProjectNo, ProjectName, ProjectDesc, StartDate FROM Project WHERE ProjectNo = ?";

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, projectNo);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int projectID = resultSet.getInt("ProjectID");
                    String projectName = resultSet.getString("ProjectName");
                    String projectDesc = resultSet.getString("ProjectDesc");
                    Date startDate = resultSet.getDate("StartDate");

                    project = new Project(projectID, projectNo, projectName, projectDesc, startDate);
                } else {
                    throw new DaoException("Project with ProjectNo " + projectNo + " not found.");
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Error retrieving project by ProjectNo", e);
        }

        return project;
    }

    // RETRIEVE INFORMATION ABOUT ALL PROJECTS
    public List<Project> getAllProjects() throws DaoException {
        List<Project> projects = new ArrayList<>();
        String query = "SELECT ProjectID, ProjectNo, ProjectName, ProjectDesc, StartDate FROM Project";

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int projectID = resultSet.getInt("ProjectID");
                int projectNo = resultSet.getInt("ProjectNo");
                String projectName = resultSet.getString("ProjectName");
                String projectDesc = resultSet.getString("ProjectDesc");
                Date startDate = resultSet.getDate("StartDate");

                projects.add(new Project(projectID, projectNo, projectName, projectDesc, startDate));
            }
        } catch (SQLException e) {
            throw new DaoException("Error retrieving all projects", e);
        }

        return projects;
    }

    // UPDATE PROJECT INFORMATION
    public void updateProject(Project project) throws DaoException {
        String query = "UPDATE Project SET ProjectNo = ?, ProjectName = ?, ProjectDesc = ?, StartDate = ? WHERE ProjectID = ?";

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, project.getProjectNo());
            statement.setString(2, project.getProjectName());
            statement.setString(3, project.getProjectDescription());
            statement.setDate(4, project.getProjectStartDate());
            statement.setInt(5, project.getProjectID());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error updating project", e);
        }
    }

    // RETRIEVE THE TOTAL NUMBER OF MILESTONES FOR A CERTAIN PROJECT
    public int getNumberOfMilestones(int projectNo) throws DaoException {
        int numberOfMilestones = 0;
        String query = "SELECT COUNT(*) AS MilestoneCount FROM Milestone WHERE ProjectID = ?";

        try (Connection connection = connectionHandler.getConnection()) {

            // Get ProjectID from ProjectNo
            int projectID = getProjectIDByProjectNo(projectNo, connection);
            if (projectID == -1) {
                throw new DaoException("Project with ProjectNo " + projectNo + " not found.");
            }

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, projectID);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        numberOfMilestones = resultSet.getInt("MilestoneCount");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Error retrieving number of milestones", e);
        }

        return numberOfMilestones;
    }

    // RETRIEVE THE PROJECTS THAT HAVE ALL EMPLOYEES ASSIGNED TO IT
    public List<Project> getProjectsAllEmployeesAssigned() throws DaoException {
        List<Project> projects = new ArrayList<>();
        String query = "SELECT p.ProjectID, p.ProjectNo, p.ProjectName, p.ProjectDesc, p.StartDate " + 
                "FROM Project p " +
                "WHERE NOT EXISTS ( " +
                "    SELECT 1 FROM Employee e " +
                "    WHERE NOT EXISTS ( " +
                "        SELECT 1 FROM Work w " +
                "        WHERE w.ProjectID = p.ProjectID AND w.EmployeeID = e.EmployeeID " + // Join condition
                "    ) " +
                ")";

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int projectID = resultSet.getInt("ProjectID");
                int projectNo = resultSet.getInt("ProjectNo");
                String projectName = resultSet.getString("ProjectName");
                String projectDesc = resultSet.getString("ProjectDesc");
                Date startDate = resultSet.getDate("StartDate");

                projects.add(new Project(projectID, projectNo, projectName, projectDesc, startDate));
            }
        } catch (SQLException e) {
            throw new DaoException("Error retrieving projects with all employees assigned", e);
        }

        return projects;
    }

    // SEARCH PROJECTS
    public List<Project> searchProjects(String searchCriteria, String searchTerm) throws DaoException {
        List<Project> projects = new ArrayList<>();
        String query = "SELECT ProjectID, ProjectNo, ProjectName, ProjectDesc, StartDate FROM Project WHERE "
                + searchCriteria + " LIKE ?";

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, "%" + searchTerm + "%"); // Wildcard search that uses special characters to 
                                                                          // replace one or more characters in a string
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int projectID = resultSet.getInt("ProjectID");
                    int projectNo = resultSet.getInt("ProjectNo");
                    String projectName = resultSet.getString("ProjectName");
                    String projectDesc = resultSet.getString("ProjectDesc");
                    Date startDate = resultSet.getDate("StartDate");

                    projects.add(new Project(projectID, projectNo, projectName, projectDesc, startDate));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Error searching projects", e);
        }

        return projects;
    }

    // DELETE A PROJECT BY ProjectNo
    public void deleteProject(int projectNo) throws DaoException {
        String deleteProjectQuery = "DELETE FROM Project WHERE ProjectID = ?";
        String deleteWorkQuery = "DELETE FROM Work WHERE ProjectID = ?";
        String deleteMilestoneQuery = "DELETE FROM Milestone WHERE ProjectID = ?";

        try (Connection connection = connectionHandler.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            // Get ProjectID from ProjectNo
            int projectID = getProjectIDByProjectNo(projectNo, connection);
            if (projectID == -1) {
                connection.rollback();
                throw new DaoException("Project with ProjectNo " + projectNo + " not found.");
            }

            try (PreparedStatement workStatement = connection.prepareStatement(deleteWorkQuery);
                    PreparedStatement milestoneStatement = connection.prepareStatement(deleteMilestoneQuery);
                    PreparedStatement projectStatement = connection.prepareStatement(deleteProjectQuery)) {

                // Delete from Work table
                workStatement.setInt(1, projectID);
                workStatement.executeUpdate();

                // Delete from Milestone table
                milestoneStatement.setInt(1, projectID);
                milestoneStatement.executeUpdate();

                // Delete from Project table
                projectStatement.setInt(1, projectID);
                int projectRowsAffected = projectStatement.executeUpdate();

                if (projectRowsAffected > 0) {
                    connection.commit(); // Commit if the project record was deleted
                } else {
                    connection.rollback(); // Rollback if nothing was deleted
                    // Rollback makes sure that the database is not left in an inconsistent state
                    throw new DaoException("No records found for ProjectNo: " + projectNo);
                }

            } catch (SQLException e) {
                connection.rollback(); // Rollback in case of exception
                throw new DaoException("Error deleting project", e);
            }
        } catch (SQLException e) {
            throw new DaoException("Error obtaining connection", e);
        }
    }

    // HELPER METHOD TO GET PROJECT BY PROJECTID
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

    // GENERATE NEXT PROJECTNO
    public int getNextProjectNo() throws DaoException {
        String query = "SELECT MAX(ProjectNo) AS MaxProjectNo FROM Project";
        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                int maxProjectNo = resultSet.getInt("MaxProjectNo");
                return maxProjectNo + 1;
            } else {
                return 1; // If no projects exist, start from 1
            }
        } catch (SQLException e) {
            throw new DaoException("Error generating next ProjectNo", e);
        }
    }

}
