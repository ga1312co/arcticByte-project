package se.lu.ics.data;

import se.lu.ics.models.Employee;
import se.lu.ics.models.Project;
import se.lu.ics.models.Work;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorkDao {

    private ConnectionHandler connectionHandler;

    public WorkDao(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    // ADD A NEW WORK RECORD "REGISTERWORK"
    public void registerWork(Work work) throws DaoException {
        String query = "INSERT INTO Work (EmployeeID, ProjectID, AssignedHours, HoursWorked) VALUES (?, ?, ?, ?)";

        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            if (work == null) {
                throw new DaoException("Work cannot be null");
            }

            // Get EmployeeID from EmployeeNo
            int employeeID = getEmployeeIDByEmployeeNo(work.getEmployee().getEmployeeNo(), connection);
            if (employeeID == -1) {
                throw new DaoException("Employee with EmployeeNo " + work.getEmployee().getEmployeeNo() + " not found.");
            }

            // Get ProjectID from ProjectNo
            int projectID = getProjectIDByProjectNo(work.getProject().getProjectNo(), connection);
            if (projectID == -1) {
                throw new DaoException("Project with ProjectNo " + work.getProject().getProjectNo() + " not found.");
            }

            statement.setInt(1, employeeID);
            statement.setInt(2, projectID);
            statement.setInt(3, work.getAssignedHours());
            statement.setInt(4, work.getHoursWorked());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error adding work record", e);
        }
    }

    // CONNECT A CONSULTANT TO A PROJECT
    public void connectConsultantToProject(int employeeNo, int projectNo) throws DaoException {
        String query = "INSERT INTO Work (EmployeeID, ProjectID, AssignedHours, HoursWorked) VALUES (?, ?, 0, 0)";
    
        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
    
            // Get EmployeeID from EmployeeNo
            int employeeID = getEmployeeIDByEmployeeNo(employeeNo, connection);
            if (employeeID == -1) {
                throw new DaoException("Employee with EmployeeNo " + employeeNo + " not found.");
            }

            // Get ProjectID from ProjectNo
            int projectID = getProjectIDByProjectNo(projectNo, connection);
            if (projectID == -1) {
                throw new DaoException("Project with ProjectNo " + projectNo + " not found.");
            }

            statement.setInt(1, employeeID);
            statement.setInt(2, projectID);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error connecting consultant to project", e);
        }
    }

    // DISPLAY THE TOTAL NUMBER OF HOURS WORKED BY ALL CONSULTANTS ACROSS ALL PROJECTS
    public int getTotalHoursWorked() throws DaoException {
        int totalHours = 0; // Initialize total hours to 0
        String query = "SELECT SUM(HoursWorked) AS TotalHours FROM Work";
    
        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
    
            if (resultSet.next()) { // The if (resultSet.next()) ensures that you're safely 
            // checking if there is data to retrieve.
            // Without it, you might try to read from an empty result set, leading to errors.
                totalHours = resultSet.getInt("TotalHours");
            }
        } catch (SQLException e) {
            throw new DaoException("Error retrieving total hours worked", e);
        }
        return totalHours;
    }

    // RETRIEVE THE TOTAL NUMBER OF HOURS WORKED BY A SPECIFIC EMPLOYEE
    public int getTotalHoursWorkedByEmployee(int employeeNo) throws DaoException {
        String query = "SELECT SUM(HoursWorked) AS TotalHours FROM Work WHERE EmployeeID = ?";
        int totalHours = 0; // Initialize total hours to 0

        try (Connection connection = connectionHandler.getConnection()) {
            int employeeID = getEmployeeIDByEmployeeNo(employeeNo, connection);
            if (employeeID == -1) {
                throw new DaoException("Employee with EmployeeNo " + employeeNo + " not found.");
            }

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, employeeID);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        totalHours = resultSet.getInt("TotalHours");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Error retrieving total hours worked by employee", e);
        }
        return totalHours;
    }

    // RETRIEVE FILTERED WORK RECORDS
    public List<Work> getFilteredWork(Employee employee, Project project) throws DaoException {
        List<Work> workList = new ArrayList<>(); // Create a list to store work records
        StringBuilder query = new StringBuilder("SELECT EmployeeID, ProjectID, AssignedHours, HoursWorked FROM Work WHERE 1=1");
        List<Integer> parameters = new ArrayList<>(); // Create a list to store parameters

        try (Connection connection = connectionHandler.getConnection()) {

            if (employee != null) {
                int employeeID = getEmployeeIDByEmployeeNo(employee.getEmployeeNo(), connection);
                if (employeeID == -1) {
                    throw new DaoException("Employee with EmployeeNo " + employee.getEmployeeNo() + " not found.");
                }
                query.append(" AND EmployeeID = ?"); // Append the EmployeeID to the query
                parameters.add(employeeID); // Add the EmployeeID to the parameters list
            }

            if (project != null) {
                int projectID = getProjectIDByProjectNo(project.getProjectNo(), connection);
                if (projectID == -1) {
                    throw new DaoException("Project with ProjectNo " + project.getProjectNo() + " not found.");
                }
                query.append(" AND ProjectID = ?"); // Append the ProjectID to the query
                parameters.add(projectID); // Add the ProjectID to the parameters list
            }

            try (PreparedStatement statement = connection.prepareStatement(query.toString())) {
                for (int i = 0; i < parameters.size(); i++) { // Loop through the parameters list
                    statement.setInt(i + 1, parameters.get(i)); // Set the parameter values
                }

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        int employeeID = resultSet.getInt("EmployeeID");
                        int projectID = resultSet.getInt("ProjectID");
                        int assignedHours = resultSet.getInt("AssignedHours");
                        int hoursWorked = resultSet.getInt("HoursWorked");

                        Employee emp = getEmployeeByEmployeeID(employeeID, connection);
                        Project proj = getProjectByProjectID(projectID, connection);

                        workList.add(new Work(emp, proj, assignedHours, hoursWorked));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Error retrieving filtered work records", e);
        }

        return workList;
    }

    // DELETE A WORK RECORD
    public void deleteWork(int employeeNo, int projectNo) throws DaoException {
        String query = "DELETE FROM Work WHERE EmployeeID = ? AND ProjectID = ?";

        try (Connection connection = connectionHandler.getConnection()) {

            // Get EmployeeID from EmployeeNo
            int employeeID = getEmployeeIDByEmployeeNo(employeeNo, connection);
            if (employeeID == -1) { // If employeeID is -1, the employee is not found
                throw new DaoException("Employee with EmployeeNo " + employeeNo + " not found.");
            }

            // Get ProjectID from ProjectNo
            int projectID = getProjectIDByProjectNo(projectNo, connection);
            if (projectID == -1) { // If projectID is -1, the project is not found
                throw new DaoException("Project with ProjectNo " + projectNo + " not found.");
            }

            try (PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setInt(1, employeeID);
                statement.setInt(2, projectID);

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoException("Error deleting work record", e);
        }
    }

    // UPDATE A WORK RECORD
    public void updateWork(Work work) throws DaoException {
        String query = "UPDATE Work SET AssignedHours = ?, HoursWorked = ? WHERE EmployeeID = ? AND ProjectID = ?";

        try (Connection connection = connectionHandler.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {

            if (work == null) {
                throw new DaoException("Work cannot be null");
            }

            // Get EmployeeID from EmployeeNo
            int employeeID = getEmployeeIDByEmployeeNo(work.getEmployee().getEmployeeNo(), connection);
            if (employeeID == -1) {
                throw new DaoException("Employee with EmployeeNo " + work.getEmployee().getEmployeeNo() + " not found.");
            }

            // Get ProjectID from ProjectNo
            int projectID = getProjectIDByProjectNo(work.getProject().getProjectNo(), connection);
            if (projectID == -1) {
                throw new DaoException("Project with ProjectNo " + work.getProject().getProjectNo() + " not found.");
            }

            statement.setInt(1, work.getAssignedHours());
            statement.setInt(2, work.getHoursWorked());
            statement.setInt(3, employeeID);
            statement.setInt(4, projectID);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error updating work record", e);
        }
    }

    // RETRIEVE THE HARDEST WORKING CONSULTANT BY HIGHEST AMOUNT OF ASSIGNED HOURS ACROSS ALL PROJECTS
    public Employee getHardestWorkingConsultant() throws DaoException {
        Employee hardestWorkingConsultant = null;
        String query = "SELECT TOP 1 EmployeeID, SUM(AssignedHours) AS TotalAssignedHours " +
                       "FROM Work " +
                       "GROUP BY EmployeeID " +
                       "ORDER BY TotalAssignedHours DESC";
        
        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
    
            if (resultSet.next()) {
                int employeeID = resultSet.getInt("EmployeeID");
                // Get Employee by EmployeeID
                hardestWorkingConsultant = getEmployeeByEmployeeID(employeeID, connection);
            }
        } catch (SQLException e) {
            throw new DaoException("Error retrieving hardest working consultant", e);
        }
        return hardestWorkingConsultant;
    }
    

    // RETRIEVE ALL WORK RECORDS
    public List<Work> getAllWork() throws DaoException {
        List<Work> workList = new ArrayList<>();
        String query = "SELECT EmployeeID, ProjectID, AssignedHours, HoursWorked FROM Work";

        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int employeeID = resultSet.getInt("EmployeeID");
                int projectID = resultSet.getInt("ProjectID");
                int assignedHours = resultSet.getInt("AssignedHours");
                int hoursWorked = resultSet.getInt("HoursWorked");

                Employee employee = getEmployeeByEmployeeID(employeeID, connection); 
                Project project = getProjectByProjectID(projectID, connection);

                workList.add(new Work(employee, project, assignedHours, hoursWorked)); 
            }
        } catch (SQLException e) {
            throw new DaoException("Error retrieving all work records", e);
        }
        return workList;
    }

    // COUNT THE NUMBER OF PROJECTS FOR A SPECIFIC EMPLOYEE
    public int countProjectsForEmployee(int employeeNo) throws DaoException {
        String query = "SELECT COUNT(*) FROM Work WHERE EmployeeID = ?";
        
        try (Connection connection = connectionHandler.getConnection()) {
             
            // Get EmployeeID from EmployeeNo
            int employeeID = getEmployeeIDByEmployeeNo(employeeNo, connection);
            if (employeeID == -1) {
                throw new DaoException("Employee with EmployeeNo " + employeeNo + " not found.");
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, employeeID);
                ResultSet resultSet = preparedStatement.executeQuery();
                
                if (resultSet.next()) {
                    return resultSet.getInt(1); // Return the count of projects
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Error counting projects for employee", e);
        }
        
        return 0; // Default return if no projects
    }
    
    // GET THE TOTAL NUMBER OF HOURS WORKED ON A SPECIFIC PROJECT
    public int getTotalProjectHours(int projectNo) throws DaoException {
        String query = "SELECT SUM(AssignedHours) AS TotalHours FROM Work WHERE ProjectID = ?";
        int totalHours = 0;
    
        try (Connection connection = connectionHandler.getConnection()) {

            // Get ProjectID from ProjectNo
            int projectID = getProjectIDByProjectNo(projectNo, connection);
            if (projectID == -1) {
                throw new DaoException("Project with ProjectNo " + projectNo + " not found.");
            }
    
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, projectID);
                ResultSet resultSet = preparedStatement.executeQuery();
    
                if (resultSet.next()) {
                    totalHours = resultSet.getInt("TotalHours");
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching total project hours", e);
        }
    
        return totalHours;
    }

    // HELPER METHOD TO GET EMPLOYEEID BY EMPLOYEENO
    private int getEmployeeIDByEmployeeNo(int employeeNo, Connection connection) throws SQLException {
        String query = "SELECT EmployeeID FROM Employee WHERE EmployeeNo = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, employeeNo);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("EmployeeID");
                } else {
                    return -1; // Employee not found
                }
            }
        }
    }

    // HELPER METHOD TO GET EMPLOYEE BY EMPLOYEEID
    private Employee getEmployeeByEmployeeID(int employeeID, Connection connection) throws SQLException, DaoException {
        String query = "SELECT EmployeeID, EmployeeNo, EmpFirstName, EmpLastName, EmpTitle, EmpAddress FROM Employee WHERE EmployeeID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, employeeID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int employeeNo = resultSet.getInt("EmployeeNo");
                    String empFirstName = resultSet.getString("EmpFirstName");
                    String empLastName = resultSet.getString("EmpLastName");
                    String empTitle = resultSet.getString("EmpTitle");
                    String empAddress = resultSet.getString("EmpAddress");
                    return new Employee(employeeID, employeeNo, empFirstName, empLastName, empTitle, empAddress);
                } else {
                    throw new DaoException("Employee with EmployeeID " + employeeID + " not found.");
                }
            }
        }
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
