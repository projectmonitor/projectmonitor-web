package com.projectmonitor;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application.properties")
@ConfigurationProperties
@Getter
@Setter
public class ApplicationConfiguration {
    private String ciUrl;
    private String pivotalTrackerStoryDetailsUrl;
    private String storyAcceptanceUrl;
    private String productionUrl;
    private String githubUsername;
    private String githubProjectName;
}
