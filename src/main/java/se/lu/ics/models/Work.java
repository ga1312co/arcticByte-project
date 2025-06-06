package se.lu.ics.models;

public class Work {
    private int hoursWorked;
    private int assignedHours;
    private Employee employee;
    private Project project;

    
    public Work(Employee employee, Project project, int assignedHours, int hoursWorked) {
        this.employee = employee;
        this.project = project;
        this.assignedHours = assignedHours;
        this.hoursWorked = hoursWorked;
    }

    public int getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(int hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public int getAssignedHours() {
        return assignedHours;
    }

    public void setAssignedHours(int assignedHours) {
        this.assignedHours = assignedHours;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }



}

