package com.projectmonitor.pivotaltracker;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
@Getter
@Setter
class PivotalTrackerStoryConfiguration {
    private String pivotalTrackerStoryDetailsUrl;
    private String trackerProjectId;
    private String pivotalTrackerToken;
}
