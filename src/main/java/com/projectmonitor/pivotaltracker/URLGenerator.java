package com.projectmonitor.pivotaltracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class URLGenerator {
    private PivotalTrackerStoryConfiguration pivotalTrackerStoryConfiguration;

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public URLGenerator(PivotalTrackerStoryConfiguration pivotalTrackerStoryConfiguration) {
        this.pivotalTrackerStoryConfiguration = pivotalTrackerStoryConfiguration;
    }

    String generate(String pivotalTrackerStoryID) {
        String storyURL = pivotalTrackerStoryConfiguration.getPivotalTrackerStoryDetailsUrl();
        storyURL = storyURL.replace("{STORY_ID}", pivotalTrackerStoryID);
        storyURL = storyURL.replace("{TRACKER_PROJECT_ID}", pivotalTrackerStoryConfiguration.getTrackerProjectId());
        return storyURL;
    }
}
