package com.projectmonitor.jenkins;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
@Getter
@Setter
public class CIJobConfiguration {

    private String ciLastCompletedBuildURL;

    private String storyAcceptanceDeployJobURL;
    private String storyAcceptanceDeployStatusURL;
    private String storyAcceptanceDeployJobLastStatusURL;

    private String productionDeployJobURL;
    private String productionDeployStatusURL;
    private String productionDeployJobLastStatusURL;
    
    private String ciUsername;
    private String ciPassword;

    private String revertProductionURL;
    private String revertProductionStatusURL;

    private String revertStoryAcceptanceURL;
    private String revertStoryAcceptanceStatusURL;
}
