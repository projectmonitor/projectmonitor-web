package com.projectmonitor.productionrelease;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class PivotalTrackerStoryConfiguration {
    private String pivotalTrackerStoryDetailsUrl;

    private String trackerProjectId;
    private String pivotalTrackerToken;

    public String getPivotalTrackerStoryDetailsUrl() {
        return pivotalTrackerStoryDetailsUrl;
    }

    public void setPivotalTrackerStoryDetailsUrl(String pivotalTrackerStoryDetailsUrl) {
        this.pivotalTrackerStoryDetailsUrl = pivotalTrackerStoryDetailsUrl;
    }

    public String getTrackerProjectId() {
        return trackerProjectId;
    }

    public void setTrackerProjectId(String trackerProjectId) {
        this.trackerProjectId = trackerProjectId;
    }

    public String getPivotalTrackerToken() {
        return pivotalTrackerToken;
    }

    public void setPivotalTrackerToken(String pivotalTrackerToken) {
        this.pivotalTrackerToken = pivotalTrackerToken;
    }
}
