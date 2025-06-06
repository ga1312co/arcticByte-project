package se.lu.ics.models;

import java.sql.Date;

public class Project {
    private int projectID;          // Surrogate key used for database operations
    private int projectNo;          // Natural key displayed in the GUI
    private String projectName;
    private String projectDescription;
    private Date projectStartDate;

    // Constructor with projectID and projectNo - when reading from database
    public Project(int projectID, int projectNo, String projectName, String projectDescription, Date projectStartDate) {
        this.projectID = projectID;
        this.projectNo = projectNo;
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.projectStartDate = projectStartDate;
    }

    // Constructor without projectID - when adding new project
    public Project(int projectNo, String projectName, String projectDescription, Date projectStartDate) {
        this.projectNo = projectNo;
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.projectStartDate = projectStartDate;
    }

    @Override
    public String toString() {
        return projectNo + " - " + projectName;
    }

    // Getters and Setters
    public int getProjectID() {
        return projectID;
    }

    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    public int getProjectNo() {
        return projectNo;
    }

    public void setProjectNo(int projectNo) {
        this.projectNo = projectNo;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public Date getProjectStartDate() {
        return projectStartDate;
    }

    public void setProjectStartDate(Date projectStartDate) {
        this.projectStartDate = projectStartDate;
    }
}
