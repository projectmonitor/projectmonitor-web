package com.projectmonitor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectMonitorTrackerStoryInfo {

    private String pivotalTrackerStoryID;


    public String getPivotalTrackerStoryID() {
        return pivotalTrackerStoryID;
    }

    public void setPivotalTrackerStoryID(String pivotalTrackerStoryID) {
        this.pivotalTrackerStoryID = pivotalTrackerStoryID;
    }

}
