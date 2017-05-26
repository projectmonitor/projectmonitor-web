package com.projectmonitor.jenkins;

import com.projectmonitor.deploys.Deploy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JenkinsJobs implements JenkinsAPI {

    private JenkinsClient jenkinsClient;
    private CIJobConfiguration ciJobConfiguration;
    private JenkinsRevertJob jenkinsRevertJob;
    private JenkinsRevertStoryAcceptance jenkinsRevertStoryAcceptance;

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public JenkinsJobs(JenkinsClient jenkinsClient,
                       CIJobConfiguration ciJobConfiguration,
                       JenkinsRevertJob jenkinsRevertJob,
                       JenkinsRevertStoryAcceptance jenkinsRevertStoryAcceptance) {
        this.jenkinsClient = jenkinsClient;
        this.ciJobConfiguration = ciJobConfiguration;
        this.jenkinsRevertJob = jenkinsRevertJob;
        this.jenkinsRevertStoryAcceptance = jenkinsRevertStoryAcceptance;
    }

    @Override
    public CIResponse loadLastCompletedCIRun() {
        CIResponse ciResponse = new CIResponse();
        try {
            ciResponse = jenkinsClient.loadJobStatus(
                    ciJobConfiguration.getCiLastCompletedBuildURL());
        } catch (RequestFailedException exception) {
            logger.warn(exception.getMessage(), exception);
            ciResponse.setResult("CI is not responding");
        }
        return ciResponse;
    }

    @Override
    public CIResponse loadStoryAcceptanceLastDeployStatus() {
        CIResponse ciResponse = new CIResponse();
        try {
            ciResponse = jenkinsClient.loadJobStatus(
                    ciJobConfiguration.getStoryAcceptanceDeployJobLastStatusURL()
            );
        } catch (RequestFailedException exception) {
            logger.warn(exception.getMessage(), exception);
            ciResponse.setResult("Story Acceptance Deploy Job is not responding");
        }

        return ciResponse;
    }

    @Override
    public CIResponse loadProductionLastDeployStatus() {
        CIResponse ciResponse = new CIResponse();
        try {
            ciResponse = jenkinsClient.loadJobStatus(
                    ciJobConfiguration.getProductionDeployJobLastStatusURL()
            );
        } catch (RequestFailedException exception) {
            logger.warn(exception.getMessage(), exception);
            ciResponse.setResult("Production Deploy Job is not responding");
        }

        return ciResponse;
    }

    @Override
    public void revertProduction(Deploy deploy) throws RevertFailedException {
        jenkinsRevertJob.execute(deploy);
    }

    @Override
    public void revertAcceptance(Deploy deploy) throws RevertFailedException {
        jenkinsRevertStoryAcceptance.execute(deploy);
    }
}
