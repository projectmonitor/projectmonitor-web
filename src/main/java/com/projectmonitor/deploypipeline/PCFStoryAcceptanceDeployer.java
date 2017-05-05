package com.projectmonitor.deploypipeline;

import com.projectmonitor.jenkins.CIJobConfiguration;
import com.projectmonitor.jenkins.JenkinsJobStatus;
import com.projectmonitor.jenkins.JenkinsRestTemplate;
import com.projectmonitor.pivotaltracker.PivotalTrackerAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PCFStoryAcceptanceDeployer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private final StoryAcceptanceQueue storyAcceptanceQueue;
    private final JenkinsRestTemplate jenkinsRestTemplate;
    private final CIJobConfiguration ciJobConfiguration;
    private final ThreadSleepService threadSleepService;
    private PivotalTrackerAPI pivotalTrackerAPI;
    private static final String JENKINS_SUCCESS_MESSAGE = "SUCCESS";

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

    public void pushRejectedBuild(String rejectedStoryID) {
        logger.info("Story in acceptance is rejected, storyID: {}", rejectedStoryID);
        Deploy nextDeploy = storyAcceptanceQueue.readHead();
        if (!Objects.equals(rejectedStoryID, nextDeploy.getStoryID())) {
            return;
        }

        push();
        pivotalTrackerAPI.removeRejectLabel(rejectedStoryID);
    }
}
