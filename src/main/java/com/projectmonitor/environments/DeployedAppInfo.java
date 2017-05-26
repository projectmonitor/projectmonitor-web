package com.projectmonitor.environments;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeployedAppInfo {

    private String pivotalTrackerStoryID;

    private String storySHA;


    public String getPivotalTrackerStoryID() {
        return pivotalTrackerStoryID;
    }

    public void setPivotalTrackerStoryID(String pivotalTrackerStoryID) {
        this.pivotalTrackerStoryID = pivotalTrackerStoryID;
    }

    public String getStorySHA() {
        return storySHA;
    }

    public void setStorySHA(String storySHA) {
        this.storySHA = storySHA;
    }
}
