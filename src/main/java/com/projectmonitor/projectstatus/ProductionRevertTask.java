package com.projectmonitor.projectstatus;

import com.projectmonitor.deploypipeline.Deploy;
import com.projectmonitor.deploypipeline.ProductionDeployHistory;
import com.projectmonitor.deploypipeline.StoryAcceptanceQueue;
import com.projectmonitor.jenkins.JenkinsAPI;
import com.projectmonitor.jenkins.RevertFailedException;
import com.projectmonitor.pivotaltracker.PivotalTrackerAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ProductionRevertTask {

    private ProductionDeployHistory productionDeployHistory;
    private StoryAcceptanceQueue storyAcceptanceQueue;
    private Environments environments;
    private PivotalTrackerAPI pivotalTrackerAPI;
    private JenkinsAPI jenkinsAPI;
    private ProductionRevertFlag productionRevertFlag;
    private RevertErrorRepository revertErrorRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public ProductionRevertTask(ProductionDeployHistory productionDeployHistory,
                                StoryAcceptanceQueue storyAcceptanceQueue,
                                Environments environments,
                                PivotalTrackerAPI pivotalTrackerAPI,
                                JenkinsAPI jenkinsAPI,
                                ProductionRevertFlag productionRevertFlag,
                                RevertErrorRepository revertErrorRepository) {
        this.productionDeployHistory = productionDeployHistory;
        this.storyAcceptanceQueue = storyAcceptanceQueue;
        this.environments = environments;
        this.pivotalTrackerAPI = pivotalTrackerAPI;
        this.jenkinsAPI = jenkinsAPI;
        this.productionRevertFlag = productionRevertFlag;
        this.revertErrorRepository = revertErrorRepository;
    }

    @Async
    void start() {
        Deploy lastProductionDeploy = productionDeployHistory.getLastDeploy();
        Deploy previousProductionDeploy = productionDeployHistory.getPreviousDeploy();

        if(previousProductionDeploy == null){
            logger.info("There isn't a previous production deploy to rollback too!");
            return;
        }

        DeployedAppInfo storyAcceptanceDeploy = environments.loadStoryAcceptanceDeployStory();

        try {
            pivotalTrackerAPI.rejectStory(lastProductionDeploy.getStoryID());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("PivotalTracker API call failed, response body: {}", e.getResponseBodyAsString(), e);
        }

        try {
            jenkinsAPI.revertProduction(previousProductionDeploy); // todo: handle errors in here
            productionDeployHistory.pop(); // todo: should this be in the method above it?
        } catch (RevertFailedException e) {
            revertErrorRepository.save(e.getMessage());
            productionRevertFlag.clear();
            return;
        }

        if (deploysMatch(lastProductionDeploy, storyAcceptanceDeploy)) {
            logger.info("Produciton and Story Acceptance builds match, bypassing revert of SA");
        } else {

            pivotalTrackerAPI.finishStory(storyAcceptanceDeploy.getPivotalTrackerStoryID());

            storyAcceptanceQueue.push(storyAcceptanceDeploy.getStorySHA(), storyAcceptanceDeploy.getPivotalTrackerStoryID());

            jenkinsAPI.deployToStoryAcceptance(lastProductionDeploy);
        }

        productionRevertFlag.clear();

        // todo: Address packages
    }

    private boolean deploysMatch(Deploy lastProductionDeploy, DeployedAppInfo storyAcceptanceDeploy) {
        return Objects.equals(lastProductionDeploy.getSha(), storyAcceptanceDeploy.getStorySHA())
                && Objects.equals(lastProductionDeploy.getStoryID(), storyAcceptanceDeploy.getPivotalTrackerStoryID());
    }
}
