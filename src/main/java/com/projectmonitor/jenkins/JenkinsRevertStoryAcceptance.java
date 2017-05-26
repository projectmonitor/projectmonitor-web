package com.projectmonitor.jenkins;

import com.projectmonitor.deploys.Deploy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class JenkinsRevertStoryAcceptance {

    private JenkinsJobAPI jenkinsJobAPI;
    private CIJobConfiguration ciJobConfiguration;
    private JenkinsJobPoller jenkinsJobPoller;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    JenkinsRevertStoryAcceptance(JenkinsJobAPI jenkinsJobAPI,
                                 CIJobConfiguration ciJobConfiguration,
                                 JenkinsJobPoller jenkinsJobPoller) {
        this.jenkinsJobAPI = jenkinsJobAPI;
        this.ciJobConfiguration = ciJobConfiguration;
        this.jenkinsJobPoller = jenkinsJobPoller;
    }

    void execute(Deploy theDeploy) throws RevertFailedException {
        String jobURL = ciJobConfiguration.getRevertStoryAcceptanceURL()
                + theDeploy.getSha() + "&STORY_ID=" + theDeploy.getStoryID();

        logger.info("Triggering Revert of Story Acceptance with sha: {}", theDeploy.getSha());
        try {
            jenkinsJobAPI.triggerJob(jobURL);
        } catch (RequestFailedException e) {
            throw new RevertFailedException(e.getMessage(), e);
        }
        jenkinsJobPoller.execute(ciJobConfiguration.getRevertStoryAcceptanceStatusURL());
    }
}
