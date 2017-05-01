package com.projectmonitor.productionrelease;

import com.projectmonitor.CIJobConfiguration;
import com.projectmonitor.StoryAcceptanceDeploy.Deploy;
import com.projectmonitor.StoryAcceptanceDeploy.StoryAcceptanceQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PCFStoryAcceptanceDeployer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private final StoryAcceptanceQueue storyAcceptanceQueue;
    private final JenkinsRestTemplate jenkinsRestTemplate;
    private final CIJobConfiguration ciJobConfiguration;
    private final ThreadSleepService threadSleepService;
    private PivotalTrackerAPI pivotalTrackerAPI;
    public static final String JENKINS_SUCCESS_MESSAGE = "SUCCESS";

    @Autowired
    public PCFStoryAcceptanceDeployer(StoryAcceptanceQueue storyAcceptanceQueue,
                                      JenkinsRestTemplate jenkinsRestTemplate,
                                      CIJobConfiguration ciJobConfiguration,
                                      ThreadSleepService threadSleepService,
                                      PivotalTrackerAPI pivotalTrackerAPI) {
        this.storyAcceptanceQueue = storyAcceptanceQueue;
        this.jenkinsRestTemplate = jenkinsRestTemplate;
        this.ciJobConfiguration = ciJobConfiguration;
        this.threadSleepService = threadSleepService;
        this.pivotalTrackerAPI = pivotalTrackerAPI;
    }

    public boolean push() {
        Deploy deploy = storyAcceptanceQueue.pop();

        if (deploy == null) {
            logger.info("No build SHA's in queue, nothing to deploy to SA\n");
            return true;
        }

        logger.info("Deploying to Story Acceptance with the following SHA: " + deploy.getSha());
        jenkinsRestTemplate.addAuthentication(ciJobConfiguration.getCiUsername(), ciJobConfiguration.getCiPassword());

        try {
            jenkinsRestTemplate.postForEntity(ciJobConfiguration.getStoryAcceptanceDeployJobURL() + deploy.getSha(),
                    null,
                    String.class);
        } catch (RuntimeException e) {
            logger.info("Call to kickoff story acceptance deploy failed, cause: ", e.getMessage());
            return false;
        }

        JenkinsJobStatus jenkinsJobStatus = new JenkinsJobStatus();
        try {
            do {
                logger.info("Sleeping before next poll...");
                threadSleepService.sleep(15000);
                try {
                    jenkinsJobStatus = jenkinsRestTemplate.getForObject(ciJobConfiguration.getStoryAcceptanceDeployStatusURL(), JenkinsJobStatus.class);
                } catch (RuntimeException e) {
                    logger.info("Call to story acceptance deploy status failed, but job kicked off, continuing polling...", e.getMessage());
                    jenkinsJobStatus.setBuilding(true);
                }
            } while (jenkinsJobStatus.isBuilding());
        } catch (InterruptedException e) {
            logger.info("Some thread problem", e.getMessage());
        }

        if (JENKINS_SUCCESS_MESSAGE.equals(jenkinsJobStatus.getResult())) {
            logger.info("Story Acceptance Deploy has finished successfully!");
            return true;
        } else {
            logger.info("Story Acceptance Deploy failed, rejecting story");
            pivotalTrackerAPI.rejectStory(deploy.getStoryID());
        }

        return false;
    }
}
