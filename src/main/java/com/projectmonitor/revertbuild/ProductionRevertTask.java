package com.projectmonitor.revertbuild;

import com.projectmonitor.deploys.Deploy;
import com.projectmonitor.deploys.ProductionDeployHistory;
import com.projectmonitor.deploys.StoryAcceptanceQueue;
import com.projectmonitor.environments.DeployedAppInfo;
import com.projectmonitor.environments.Environments;
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

        if (previousProductionDeploy == null) {
            logger.info("There isn't a previous production deploy to rollback too!");
            return;
        }

        DeployedAppInfo storyAcceptanceDeploy = environments.loadStoryAcceptanceDeployedAppInfo();

        try {
            pivotalTrackerAPI.rejectStory(lastProductionDeploy.getStoryID());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("PivotalTracker API call failed, response body: {}", e.getResponseBodyAsString(), e);
        }

        try {
            jenkinsAPI.revertProduction(previousProductionDeploy);
            productionDeployHistory.pop();
        } catch (RevertFailedException e) {
            revertErrorRepository.save(e.getMessage());
            productionRevertFlag.clear();
            return;
        }

        if (deploysMatch(lastProductionDeploy, storyAcceptanceDeploy)) {
            logger.info("Production and Story Acceptance builds match, bypassing revert of SA");
        } else {
            pivotalTrackerAPI.finishStory(storyAcceptanceDeploy.getPivotalTrackerStoryID());

            storyAcceptanceQueue.push(storyAcceptanceDeploy.getStorySHA(), storyAcceptanceDeploy.getPivotalTrackerStoryID());

            try {
                jenkinsAPI.revertAcceptance(lastProductionDeploy);
            } catch (RevertFailedException e) {
                revertErrorRepository.save(e.getMessage());
            }
        }

        productionRevertFlag.clear();
    }

    private boolean deploysMatch(Deploy lastProductionDeploy, DeployedAppInfo storyAcceptanceDeploy) {
        return Objects.equals(lastProductionDeploy.getSha(), storyAcceptanceDeploy.getStorySHA())
                && Objects.equals(lastProductionDeploy.getStoryID(), storyAcceptanceDeploy.getPivotalTrackerStoryID());
    }
}
