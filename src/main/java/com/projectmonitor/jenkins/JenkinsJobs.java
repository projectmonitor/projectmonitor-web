package com.projectmonitor.jenkins;

import com.projectmonitor.deploypipeline.Deploy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JenkinsJobs implements JenkinsAPI {

    private JenkinsJobAPI jenkinsJobAPI;
    private CIJobConfiguration ciJobConfiguration;
    private JenkinsRevertJob jenkinsRevertJob;
    private JenkinsRevertStoryAcceptance jenkinsRevertStoryAcceptance;

    @Autowired
    public JenkinsJobs(JenkinsJobAPI jenkinsJobAPI,
                       CIJobConfiguration ciJobConfiguration,
                       JenkinsRevertJob jenkinsRevertJob,
                       JenkinsRevertStoryAcceptance jenkinsRevertStoryAcceptance) {
        this.jenkinsJobAPI = jenkinsJobAPI;
        this.ciJobConfiguration = ciJobConfiguration;
        this.jenkinsRevertJob = jenkinsRevertJob;
        this.jenkinsRevertStoryAcceptance = jenkinsRevertStoryAcceptance;
    }

    @Override
    public CIResponse loadLastCompletedCIRun() {
        CIResponse ciResponse = new CIResponse();
        try {
            ciResponse = jenkinsJobAPI.loadJobStatus(
                    ciJobConfiguration.getCiLastCompletedBuildURL());
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            ciResponse.setResult("CI is not responding");
        }
        return ciResponse;
    }

    @Override
    public CIResponse loadStoryAcceptanceLastDeployStatus() {
        CIResponse ciResponse = new CIResponse();
        try {
            ciResponse = jenkinsJobAPI.loadJobStatus(
                    ciJobConfiguration.getStoryAcceptanceDeployJobLastStatusURL()
            );
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            ciResponse.setResult("Story Acceptance Deploy Job is not responding");
        }

        return ciResponse;
    }

    @Override
    public CIResponse loadProductionLastDeployStatus() {
        CIResponse ciResponse = new CIResponse();
        try {
            ciResponse = jenkinsJobAPI.loadJobStatus(
                    ciJobConfiguration.getProductionDeployJobLastStatusURL()
            );
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            ciResponse.setResult("Production Deploy Job is not responding");
        }

        return ciResponse;
    }

    @Override
    public void revertProduction(Deploy deploy) throws RevertFailedException {
        jenkinsRevertJob.execute(deploy);
    }

    @Override
    public void deployToStoryAcceptance(Deploy deploy) {
        jenkinsRevertStoryAcceptance.execute(deploy);
    }
}
