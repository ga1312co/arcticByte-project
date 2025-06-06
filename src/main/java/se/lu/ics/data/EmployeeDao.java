package se.lu.ics.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import se.lu.ics.models.Employee;

public class EmployeeDao {

    private ConnectionHandler connectionHandler;

    // Constructor to accept ConnectionHandler
    public EmployeeDao(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    // REGISTER EMPLOYEE IN THE DATABASE
    public void registerEmployee(Employee employee) throws DaoException {
        String query = "INSERT INTO Employee (EmployeeNo, EmpFirstName, EmpLastName, EmpTitle, EmpAddress) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, employee.getEmployeeNo()); // EmployeeNo is a natural key
            preparedStatement.setString(2, employee.getEmpFirstName()); // Other fields are attributes
            preparedStatement.setString(3, employee.getEmpLastName());
            preparedStatement.setString(4, employee.getEmpTitle());
            preparedStatement.setString(5, employee.getEmpAddress());

            preparedStatement.executeUpdate(); // Execute the query
        } catch (SQLException e) {
            throw new DaoException("Error registering employee", e);
        }
    }

    // RETRIEVE INFORMATION ABOUT AN EMPLOYEE BY EmployeeNo
    public Employee findByEmployeeNo(int employeeNo) throws DaoException {
        Employee employee = null; // Initialize to null 
        //because if the employee is not found, the method will return null
        String query = "SELECT * FROM Employee WHERE EmployeeNo = ?";

        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, employeeNo); // Set the parameter in the query
            // The Paramterindex used to specify the position of the parameter in the query
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    employee = new Employee(
                            resultSet.getInt("EmployeeID"),    // Surrogate key
                            resultSet.getInt("EmployeeNo"),    // Natural key
                            resultSet.getString("EmpFirstName"),
                            resultSet.getString("EmpLastName"),
                            resultSet.getString("EmpTitle"),
                            resultSet.getString("EmpAddress"));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Error retrieving employee by EmployeeNo", e);
        }

        return employee;
    }

    // RETRIEVE INFORMATION ON ALL EMPLOYEES WITH A SPECIFIC TITLE
    public List<Employee> findByTitle(String empTitle) throws DaoException {
        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM Employee WHERE EmpTitle = ?";

        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, empTitle);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Employee employee = new Employee(
                            resultSet.getInt("EmployeeID"),
                            resultSet.getInt("EmployeeNo"),
                            resultSet.getString("EmpFirstName"),
                            resultSet.getString("EmpLastName"),
                            resultSet.getString("EmpTitle"),
                            resultSet.getString("EmpAddress"));
                    employees.add(employee);
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Error retrieving employees by title", e);
        }

        return employees;
    }

    // LIST ALL EMPLOYEES WORKING ON A SPECIFIC PROJECT
    public List<Employee> findEmployeesByProject(int projectNo) throws DaoException {
        List<Employee> employees = new ArrayList<>();
        String query = "SELECT e.EmployeeID, e.EmployeeNo, e.EmpFirstName, e.EmpLastName, e.EmpTitle, e.EmpAddress " +
                       "FROM Employee e " +
                       "JOIN Work w ON e.EmployeeID = w.EmployeeID " + // Join Employee and Work tables
                       "WHERE w.ProjectID = ?";

        try (Connection connection = connectionHandler.getConnection()) {

            // Get ProjectID from ProjectNo
            int projectID = getProjectIDByProjectNo(projectNo, connection);
            if (projectID == -1) { // If project not found
                throw new DaoException("Project with ProjectNo " + projectNo + " not found.");
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, projectID);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Employee employee = new Employee(
                                resultSet.getInt("EmployeeID"),
                                resultSet.getInt("EmployeeNo"),
                                resultSet.getString("EmpFirstName"),
                                resultSet.getString("EmpLastName"),
                                resultSet.getString("EmpTitle"),
                                resultSet.getString("EmpAddress"));
                        employees.add(employee);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Error retrieving employees by projectNo", e);
        }

        return employees;
    }

    // HELPER METHOD TO GET PROJECTID FROM PROJECTNO
    private int getProjectIDByProjectNo(int projectNo, Connection connection) throws SQLException {
        String query = "SELECT ProjectID FROM Project WHERE ProjectNo = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, projectNo);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("ProjectID");
                } else {
                    return -1;  // If the project is not found, the method will return -1
                }
            }
        }
    }

    // DISPLAY THE TOTAL NUMBER OF HOURS WORKED BY AN EMPLOYEE ACROSS ALL PROJECTS THEY ARE ASSIGNED TO
    public int getTotalHoursWorked(int employeeNo) throws DaoException {
        int totalHours = 0; // Initialize to 0
        String query = "SELECT SUM(HoursWorked) AS TotalHours FROM Work WHERE EmployeeID = ?";

        try (Connection connection = connectionHandler.getConnection()) {

            // First, get the EmployeeID from EmployeeNo
            int employeeID = getEmployeeIDByEmployeeNo(employeeNo, connection);
            if (employeeID == -1) {
                throw new DaoException("Employee with EmployeeNo " + employeeNo + " not found.");
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setInt(1, employeeID);
                try (ResultSet resultSet = preparedStatement.executeQuery()) { 
                    if (resultSet.next()) { // If the employee has worked on any projects
                        //The if (resultSet.next()) ensures that you're safely checking if there is data to retrieve.
                        //Without it, you might try to read from an empty result set, leading to errors.
                        totalHours = resultSet.getInt("TotalHours");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Error retrieving total hours worked", e);
        }

        return totalHours;
    }

    // RETRIEVE INFORMATION ABOUT ALL EMPLOYEES
    public List<Employee> getAllEmployees() throws DaoException {
        List<Employee> employees = new ArrayList<>(); // Initialize to empty list

        String query = "SELECT * FROM Employee";
        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) { // Loop through the result set
                Employee employee = new Employee( // Create a new Employee object
                        resultSet.getInt("EmployeeID"),
                        resultSet.getInt("EmployeeNo"),
                        resultSet.getString("EmpFirstName"),
                        resultSet.getString("EmpLastName"),
                        resultSet.getString("EmpTitle"),
                        resultSet.getString("EmpAddress"));
                employees.add(employee); // Add the employee to the list
            }
        } catch (SQLException e) {
            throw new DaoException("Error retrieving all employees", e);
        }

        return employees;
    }

    // FIND EMPLOYEE BY NAME
    public Employee findByName(String fullName) throws DaoException {
        String[] nameParts = fullName.split(" "); // Split the full name into first and last name
        if (nameParts.length < 2) {
            throw new DaoException("Invalid name format. Please provide both first and last name.");
        }

        String firstName = nameParts[0]; // Get the first name
        String lastName = nameParts[1]; // Get the last name

        String query = "SELECT * FROM Employee WHERE EmpFirstName = ? AND EmpLastName = ?";
        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, firstName);
            statement.setString(2, lastName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Employee(
                            resultSet.getInt("EmployeeID"),
                            resultSet.getInt("EmployeeNo"),
                            resultSet.getString("EmpFirstName"),
                            resultSet.getString("EmpLastName"),
                            resultSet.getString("EmpTitle"),
                            resultSet.getString("EmpAddress"));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Error retrieving employee by name", e);
        }

        return null; // Return null if no employee found
    }

    // UPDATE EMPLOYEE INFORMATION
    public void updateEmployee(Employee employee) throws DaoException {
        String query = "UPDATE Employee SET EmployeeNo = ?, EmpFirstName = ?, EmpLastName = ?, EmpTitle = ?, EmpAddress = ? WHERE EmployeeID = ?";

        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, employee.getEmployeeNo());
            preparedStatement.setString(2, employee.getEmpFirstName());
            preparedStatement.setString(3, employee.getEmpLastName());
            preparedStatement.setString(4, employee.getEmpTitle());
            preparedStatement.setString(5, employee.getEmpAddress());
            preparedStatement.setInt(6, employee.getEmployeeID());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error updating employee", e);
        }
    }

    // RETRIEVE THE TOTAL NUMBER OF EMPLOYEES IN THE SYSTEM
    public int getTotalEmployees() throws DaoException {
        int totalEmployees = 0; // Initialize to 0
        String query = "SELECT COUNT(*) AS TotalEmployees FROM Employee";

        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                totalEmployees = resultSet.getInt("TotalEmployees");
            }
        } catch (SQLException e) {
            throw new DaoException("Error retrieving total employees", e);
        }

        return totalEmployees;
    }

    // DELETE EMPLOYEE BY EmployeeNo
    public void deleteEmployee(int employeeNo) throws DaoException {
        String deleteEmployeeQuery = "DELETE FROM Employee WHERE EmployeeID = ?";
        String deleteWorkQuery = "DELETE FROM Work WHERE EmployeeID = ?";

        try (Connection connection = connectionHandler.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            // First, get the EmployeeID from EmployeeNo
            int employeeID = getEmployeeIDByEmployeeNo(employeeNo, connection);
            if (employeeID == -1) {
                connection.rollback(); // Rollback if employee not found
                //Undoes all changes made in the current transaction and releases any database 
                //locks currently held by this Connection object. 
                throw new DaoException("Employee with EmployeeNo " + employeeNo + " not found.");
            }

            try (PreparedStatement workStatement = connection.prepareStatement(deleteWorkQuery);
                 PreparedStatement employeeStatement = connection.prepareStatement(deleteEmployeeQuery)) {

                // Delete from Work table
                workStatement.setInt(1, employeeID);
                workStatement.executeUpdate();

                // Delete from Employee table
                employeeStatement.setInt(1, employeeID);
                int employeeRowsAffected = employeeStatement.executeUpdate();

                // Check if the employee record was deleted
                if (employeeRowsAffected > 0) {
                    connection.commit(); // Commit if the employee record was deleted
                } else {
                    connection.rollback(); // Rollback if nothing was deleted
                    throw new DaoException("No records found for EmployeeNo: " + employeeNo);
                }

            } catch (SQLException e) {
                connection.rollback(); // Rollback in case of exception
                throw new DaoException("Error deleting employee", e);
            }
        } catch (SQLException e) {
            throw new DaoException("Error obtaining connection", e);
        }
    }

    // HELPER METHOD TO GET EMPLOYEEID FROM EMPLOYEENO
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

    // GENERATE THE NEXT EMPLOYEENO
    public int getNextEmployeeNo() throws DaoException {
        String query = "SELECT MAX(EmployeeNo) AS MaxEmployeeNo FROM Employee";
        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
    
            if (resultSet.next()) {
                int maxEmployeeNo = resultSet.getInt("MaxEmployeeNo"); // Get the max EmployeeNo
                return maxEmployeeNo + 1; // Increment the max EmployeeNo by 1
            } else {
                return 1; // If no employees exist, start from 1
            }
        } catch (SQLException e) {
            throw new DaoException("Error generating next EmployeeNo", e);
        }
    }
    

}
