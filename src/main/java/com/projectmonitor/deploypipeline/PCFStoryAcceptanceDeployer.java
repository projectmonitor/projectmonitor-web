package com.projectmonitor.deploypipeline;

import com.projectmonitor.jenkins.CIJobConfiguration;
import com.projectmonitor.jenkins.JenkinsJobPoller;
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
    private PivotalTrackerAPI pivotalTrackerAPI;
    private JenkinsJobPoller jenkinsJobPoller;

    @Autowired
    public PCFStoryAcceptanceDeployer(StoryAcceptanceQueue storyAcceptanceQueue,
                                      JenkinsRestTemplate jenkinsRestTemplate,
                                      CIJobConfiguration ciJobConfiguration,
                                      PivotalTrackerAPI pivotalTrackerAPI,
                                      JenkinsJobPoller jenkinsJobPoller) {
        this.storyAcceptanceQueue = storyAcceptanceQueue;
        this.jenkinsRestTemplate = jenkinsRestTemplate;
        this.ciJobConfiguration = ciJobConfiguration;
        this.pivotalTrackerAPI = pivotalTrackerAPI;
        this.jenkinsJobPoller = jenkinsJobPoller;
    }

    public boolean push() {
        Deploy deploy = storyAcceptanceQueue.pop();

        if (deploy == null) {
            logger.info("No build SHA's in queue, nothing to deploy to SA\n");
            return true;
        }

        logger.info("Deploying to Story Acceptance with the following SHA: " + deploy.getSha());
        try {
            jenkinsRestTemplate.triggerJob(ciJobConfiguration.getStoryAcceptanceDeployJobURL() + deploy.getSha());
        } catch (RuntimeException e) {
            logger.info("Call to kickoff story acceptance deploy failed, cause: ", e.getMessage());
            return false;
        }

        boolean buildSuccessful = jenkinsJobPoller.execute(ciJobConfiguration.getStoryAcceptanceDeployStatusURL());

        if (buildSuccessful) {
            logger.info("Story Acceptance Deploy has finished successfully!");
            return true;
        }

        logger.info("Story Acceptance Deploy failed, rejecting story");
        pivotalTrackerAPI.rejectStory(deploy.getStoryID());
        return false;
    }

    void pushRejectedBuild(String rejectedStoryID) {
        logger.info("Story in acceptance is rejected, storyID: {}", rejectedStoryID);
        Deploy nextDeploy = storyAcceptanceQueue.readHead();
        if (nextDeploy == null || !Objects.equals(rejectedStoryID, nextDeploy.getStoryID())) {
            return;
        }

        push();
        pivotalTrackerAPI.removeRejectLabel(rejectedStoryID);
    }
}
