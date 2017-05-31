package com.projectmonitor.deployjob;

import com.projectmonitor.deploys.Deploy;
import com.projectmonitor.deploys.StoryAcceptanceQueue;
import com.projectmonitor.jenkins.CIJobConfiguration;
import com.projectmonitor.jenkins.JenkinsJobPoller;
import com.projectmonitor.jenkins.JenkinsClient;
import com.projectmonitor.jenkins.RequestFailedException;
import com.projectmonitor.pivotaltracker.PivotalTrackerAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
class PCFStoryAcceptanceDeployer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private final StoryAcceptanceQueue storyAcceptanceQueue;
    private final JenkinsClient jenkinsClient;
    private final CIJobConfiguration ciJobConfiguration;
    private PivotalTrackerAPI pivotalTrackerAPI;
    private JenkinsJobPoller jenkinsJobPoller;

    @Autowired
    public PCFStoryAcceptanceDeployer(StoryAcceptanceQueue storyAcceptanceQueue,
                                      JenkinsClient jenkinsClient,
                                      CIJobConfiguration ciJobConfiguration,
                                      PivotalTrackerAPI pivotalTrackerAPI,
                                      JenkinsJobPoller jenkinsJobPoller) {
        this.storyAcceptanceQueue = storyAcceptanceQueue;
        this.jenkinsClient = jenkinsClient;
        this.ciJobConfiguration = ciJobConfiguration;
        this.pivotalTrackerAPI = pivotalTrackerAPI;
        this.jenkinsJobPoller = jenkinsJobPoller;
    }

    public boolean push() {
        Deploy deploy = storyAcceptanceQueue.readHead();

        if (deploy == null) {
            logger.info("No build SHA's in queue, nothing to deploy to SA\n");
            return true;
        }

        logger.info("Deploying to Story Acceptance with the following SHA: " + deploy.getSha());
        try {
            jenkinsClient.triggerJob(ciJobConfiguration.getStoryAcceptanceDeployJobURL() + deploy.getSha());
        } catch (RuntimeException | RequestFailedException e) {
            logger.info("Call to kickoff story acceptance deploy failed, cause: ", e.getMessage());
            return false;
        }

        boolean buildSuccessful = jenkinsJobPoller.execute(ciJobConfiguration.getStoryAcceptanceDeployStatusURL());

        if (buildSuccessful) {
            logger.info("Story Acceptance Deploy has finished successfully!");
            storyAcceptanceQueue.pop();
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
