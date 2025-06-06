package se.lu.ics.models;

public class Employee {
    private int employeeID;    // Surrogate key used for database operations
    private int employeeNo;    // Natural key displayed in the GUI
    private String empFirstName;
    private String empLastName;
    private String empTitle;
    private String empAddress;

    // Constructor with EmployeeID and EmployeeNo - when reading from database
    public Employee(int employeeID, int employeeNo, String empFirstName, String empLastName, String empTitle, String empAddress) {
        this.employeeID = employeeID;
        this.employeeNo = employeeNo;
        this.empFirstName = empFirstName;
        this.empLastName = empLastName;
        this.empTitle = empTitle;
        this.empAddress = empAddress;
    }

    // Constructor without EmployeeID - when adding a new employee
    public Employee(int employeeNo, String empFirstName, String empLastName, String empTitle, String empAddress) {
        this.employeeNo = employeeNo;
        this.empFirstName = empFirstName;
        this.empLastName = empLastName;
        this.empTitle = empTitle;
        this.empAddress = empAddress;
    }

    @Override
    public String toString() {
        return employeeNo + " - " + empFirstName + " " + empLastName;
    }

    // Getters and Setters
    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }

    public int getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(int employeeNo) {
        this.employeeNo = employeeNo;
    }

    public String getEmpFirstName() {
        return empFirstName;
    }

    public void setEmpFirstName(String empFirstName) {
        this.empFirstName = empFirstName;
    }

    public String getEmpLastName() {
        return empLastName;
    }

    public void setEmpLastName(String empLastName) {
        this.empLastName = empLastName;
    }

    public String getEmpTitle() {
        return empTitle;
    }

    public void setEmpTitle(String empTitle) {
        this.empTitle = empTitle;
    }

    public String getEmpAddress() {
        return empAddress;
    }

    public void setEmpAddress(String empAddress) {
        this.empAddress = empAddress;
    }
}
