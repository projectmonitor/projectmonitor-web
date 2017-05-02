package com.projectmonitor.jenkins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JenkinsJobs {
    private JenkinsRestTemplate jenkinsRestTemplate;
    private CIJobConfiguration ciJobConfiguration;

    @Autowired
    public JenkinsJobs(JenkinsRestTemplate jenkinsRestTemplate,
                       CIJobConfiguration ciJobConfiguration) {
        this.jenkinsRestTemplate = jenkinsRestTemplate;
        this.ciJobConfiguration = ciJobConfiguration;
    }

    public CIResponse loadLastCompletedCIRun(){
        jenkinsRestTemplate.addAuthentication(ciJobConfiguration.getCiUsername(),
                ciJobConfiguration.getCiPassword());

        CIResponse ciResponse = new CIResponse();
        try {
            ciResponse = jenkinsRestTemplate.getForObject(
                    ciJobConfiguration.getCiLastCompletedBuildURL(),
                    CIResponse.class
            );
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            ciResponse.setResult("CI is not responding");
        }
        return ciResponse;
    }

    public CIResponse loadStoryAcceptanceLastDeployStatus() {
        CIResponse ciResponse = new CIResponse();
        try {
            ciResponse = jenkinsRestTemplate.getForObject(
                    ciJobConfiguration.getStoryAcceptanceDeployJobLastStatusURL(),
                    CIResponse.class
            );
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            ciResponse.setResult("Story Acceptance Deploy Job is not responding");
        }

        return ciResponse;
    }

    public CIResponse loadProductionLastDeployStatus() {
        CIResponse ciResponse = new CIResponse();
        try {
            ciResponse = jenkinsRestTemplate.getForObject(
                    ciJobConfiguration.getProductionDeployJobLastStatusURL(),
                    CIResponse.class
            );
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            ciResponse.setResult("Production Deploy Job is not responding");

        }

        return ciResponse;
    }
}
