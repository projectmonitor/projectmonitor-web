package com.projectmonitor.jenkins;

import com.projectmonitor.deploys.Deploy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class JenkinsRevertJob {

    private CIJobConfiguration ciJobConfiguration;
    private JenkinsClient jenkinsClient;
    private JenkinsJobPoller jenkinsJobPoller;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    JenkinsRevertJob(CIJobConfiguration ciJobConfiguration,
                     JenkinsClient jenkinsClient,
                     JenkinsJobPoller jenkinsJobPoller) {
        this.ciJobConfiguration = ciJobConfiguration;
        this.jenkinsClient = jenkinsClient;
        this.jenkinsJobPoller = jenkinsJobPoller;
    }

    public void execute(Deploy theDeploy) throws RevertFailedException {
        logger.info("Reverting Production to the following SHA: " + theDeploy.getSha());
        String jobURL = ciJobConfiguration.getRevertProductionURL()
                + theDeploy.getSha() + "&STORY_ID=" + theDeploy.getStoryID();

        try {
            jenkinsClient.triggerJob(jobURL);
        } catch (RequestFailedException e) {
            throw new RevertFailedException(e.getMessage(), e);
        }

        jenkinsJobPoller.execute(ciJobConfiguration.getRevertProductionStatusURL());
    }
}
