package com.projectmonitor.deploypipeline;

import com.projectmonitor.environments.DeployedAppInfo;
import com.projectmonitor.environments.Environments;
import com.projectmonitor.pivotaltracker.PivotalTrackerAPI;
import com.projectmonitor.pivotaltracker.PivotalTrackerStory;
import com.projectmonitor.revertbuild.ProductionRevertFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DeployJob {

    private final PCFProductionDeployer pcfProductionDeployer;
    private final PCFStoryAcceptanceDeployer pcfStoryAcceptanceDeployer;
    private final PivotalTrackerAPI pivotalTrackerAPI;
    private final ProductionRevertFlag productionRevertFlag;
    private final Environments environments;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public DeployJob(PCFProductionDeployer pcfProductionDeployer,
                     PCFStoryAcceptanceDeployer pcfStoryAcceptanceDeployer,
                     PivotalTrackerAPI pivotalTrackerAPI,
                     ProductionRevertFlag productionRevertFlag,
                     Environments environments) {
        this.pcfProductionDeployer = pcfProductionDeployer;
        this.pcfStoryAcceptanceDeployer = pcfStoryAcceptanceDeployer;
        this.pivotalTrackerAPI = pivotalTrackerAPI;
        this.productionRevertFlag = productionRevertFlag;
        this.environments = environments;
    }

    @Scheduled(fixedDelay = 180000, initialDelay = 10000)
    public void execute() {
        logger.info("Job to determine if we should deploy to production kicking off...");

        if (productionRevertFlag.get()) {
            logger.info("A production revert appears to be under way. Exiting without doing anything.");
            return;
        }
        // todo: if queue is empty just bail out
        try {
            DeployedAppInfo acceptanceStory = environments.loadStoryAcceptanceDeployStory();
            logger.info("Current story in acceptance {}", acceptanceStory.getPivotalTrackerStoryID());

            DeployedAppInfo productionStory = environments.loadProductionDeployStory();
            logger.info("Current story in production: {}", productionStory.getPivotalTrackerStoryID());

            PivotalTrackerStory story = pivotalTrackerAPI.getStory(acceptanceStory.getPivotalTrackerStoryID());

            if ("accepted".equals(story.getCurrentState())) {
                if (!acceptanceStory.getPivotalTrackerStoryID().equals(productionStory.getPivotalTrackerStoryID())) {
                    if (pcfProductionDeployer.push(acceptanceStory.getStorySHA(), acceptanceStory.getPivotalTrackerStoryID())) {
                        pcfStoryAcceptanceDeployer.push();
                    }
                } else {
                    logger.info("Story in Acceptance deployed already, looking for new build to deploy to SA");
                    pcfStoryAcceptanceDeployer.push();
                }
            } else if ("rejected".equals(story.getCurrentState()) || story.isHasBeenRejected()) {
                pcfStoryAcceptanceDeployer.pushRejectedBuild(acceptanceStory.getPivotalTrackerStoryID());
            } else {
                logger.info("Story still awaiting decision. Nothing to deploy at the moment...");
            }
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            logger.info("A web call to acceptance/production or tracker errored! {}", exception.getMessage(), exception);
        }
    }
}
