package com.projectmonitor.productionrelease;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class PivotalTrackerStoryConfiguration {
    private String pivotalTrackerStoryDetailsUrl;

    public String getPivotalTrackerStoryDetailsUrl() {
        return pivotalTrackerStoryDetailsUrl;
    }

    public void setPivotalTrackerStoryDetailsUrl(String pivotalTrackerStoryDetailsUrl) {
        this.pivotalTrackerStoryDetailsUrl = pivotalTrackerStoryDetailsUrl;
    }
}
