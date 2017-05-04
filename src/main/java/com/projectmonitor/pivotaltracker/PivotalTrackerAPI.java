package com.projectmonitor.pivotaltracker;

public interface PivotalTrackerAPI {

    public PivotalTrackerStory getStory(String pivotalTrackerStoryID);

    public void rejectStory(String storyID);

    public void addRejectLabel(String storyID);
}
