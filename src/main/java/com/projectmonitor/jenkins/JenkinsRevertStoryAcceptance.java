package com.projectmonitor.jenkins;

import com.projectmonitor.deploys.Deploy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class JenkinsRevertStoryAcceptance {

    private JenkinsClient jenkinsClient;
    private CIJobConfiguration ciJobConfiguration;
    private JenkinsJobPoller jenkinsJobPoller;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    JenkinsRevertStoryAcceptance(JenkinsClient jenkinsClient,
                                 CIJobConfiguration ciJobConfiguration,
                                 JenkinsJobPoller jenkinsJobPoller) {
        this.jenkinsClient = jenkinsClient;
        this.ciJobConfiguration = ciJobConfiguration;
        this.jenkinsJobPoller = jenkinsJobPoller;
    }

    void execute(Deploy theDeploy) throws RevertFailedException {
        String jobURL = ciJobConfiguration.getRevertStoryAcceptanceURL()
                + theDeploy.getSha() + "&STORY_ID=" + theDeploy.getStoryID();

        logger.info("Triggering Revert of Story Acceptance with sha: {}", theDeploy.getSha());
        try {
            jenkinsClient.triggerJob(jobURL);
        } catch (RequestFailedException e) {
            throw new RevertFailedException(e.getMessage(), e);
        }
        jenkinsJobPoller.execute(ciJobConfiguration.getRevertStoryAcceptanceStatusURL());
    }
}
