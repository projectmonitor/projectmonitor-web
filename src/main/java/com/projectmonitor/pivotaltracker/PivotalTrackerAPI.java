package com.projectmonitor.pivotaltracker;

public interface PivotalTrackerAPI {

    PivotalTrackerStory getStory(String pivotalTrackerStoryID);

    void rejectStory(String storyID);

    void addRejectLabel(String storyID);

    void removeRejectLabel(String pivotalTrackerStoryID);

    void finishStory(String pivotalTrackerStoryID);
}
