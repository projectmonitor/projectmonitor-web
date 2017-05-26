package com.projectmonitor.jenkins;

import com.projectmonitor.deploypipeline.Deploy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class JenkinsRevertJob {

    private CIJobConfiguration ciJobConfiguration;
    private JenkinsJobAPI jenkinsJobAPI;
    private JenkinsJobPoller jenkinsJobPoller;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    JenkinsRevertJob(CIJobConfiguration ciJobConfiguration,
                     JenkinsJobAPI jenkinsJobAPI,
                     JenkinsJobPoller jenkinsJobPoller) {
        this.ciJobConfiguration = ciJobConfiguration;
        this.jenkinsJobAPI = jenkinsJobAPI;
        this.jenkinsJobPoller = jenkinsJobPoller;
    }

    public void execute(Deploy theDeploy) throws RevertFailedException {
        logger.info("Reverting Production to the following SHA: " + theDeploy.getSha());
        String jobURL = ciJobConfiguration.getRevertProductionURL()
                + theDeploy.getSha() + "&STORY_ID=" + theDeploy.getStoryID();

        try {
            jenkinsJobAPI.triggerJob(jobURL);
        } catch (RequestFailedException e) {
            throw new RevertFailedException(e.getMessage(), e);
        }

        jenkinsJobPoller.execute(ciJobConfiguration.getRevertProductionStatusURL());
    }
}
