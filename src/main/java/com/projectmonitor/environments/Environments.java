package com.projectmonitor.environments;

import com.projectmonitor.ApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class Environments {
    private ApplicationConfiguration applicationConfiguration;
    private RestTemplate restTemplate;

    @Autowired
    public Environments(ApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
        this.restTemplate = new RestTemplate();
    }

    public DeployedAppInfo loadStoryAcceptanceDeployStory() {
        DeployedAppInfo deployedAppInfo = new DeployedAppInfo();
        try {
            deployedAppInfo = restTemplate.getForObject(
                    applicationConfiguration.getStoryAcceptanceUrl(),
                    DeployedAppInfo.class
            );
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            deployedAppInfo.setPivotalTrackerStoryID("Story Acceptance is not responding");
        }

        return deployedAppInfo;
    }

    public DeployedAppInfo loadProductionDeployStory() {
        DeployedAppInfo deployedAppInfo = new DeployedAppInfo();
        try {
            deployedAppInfo = restTemplate.getForObject(
                    applicationConfiguration.getProductionUrl(),
                    DeployedAppInfo.class
            );
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            deployedAppInfo.setPivotalTrackerStoryID("Production is not responding");
        }
        return deployedAppInfo;
    }
}
