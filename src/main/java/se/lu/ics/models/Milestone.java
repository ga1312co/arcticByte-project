package se.lu.ics.models;

import java.sql.Date;

public class Milestone {
    private int milestoneID;
    private String milestoneType;
    private Date milestoneDate; 
    private Project project;

    //Constructor with MilestoneID - when reading from database
    public Milestone(int milestoneID, String milestoneType, Date milestoneDate, Project project) {
        this.milestoneID = milestoneID;
        this.milestoneType = milestoneType;
        this.milestoneDate = milestoneDate;
        this.project = project;
    }

    //Constructor without MilestoneID - when adding new milestone
    public Milestone(String milestoneType, Date milestoneDate, Project project) {
        this.milestoneType = milestoneType;
        this.milestoneDate = milestoneDate;
        this.project = project;
    }
    
    public int getMilestoneID() {
        return milestoneID;
    }

    public void setMilestoneID(int milestoneID) {
        this.milestoneID = milestoneID;
    }

    public String getMilestoneType() {
        return milestoneType;
    }

    public void setMilestoneType(String milestoneType) {
        this.milestoneType = milestoneType;
    }

    public Date getMilestoneDate() {
        return milestoneDate;
    }

    public void setMilestoneDate(Date milestoneDate) {
        this.milestoneDate = milestoneDate;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

}
