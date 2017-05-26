package com.projectmonitor.deploypipeline;

import com.projectmonitor.jenkins.CIJobConfiguration;
import com.projectmonitor.jenkins.CIResponse;
import com.projectmonitor.jenkins.JenkinsRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PCFProductionDeployer {

    private final JenkinsRestTemplate jenkinsRestTemplate;
    private final ThreadSleepService threadSleepService;
    private final CIJobConfiguration ciJobConfiguration;
    private final ProductionDeployHistory productionDeployHistory;

    static final String jenkinsSuccessMessage = "SUCCESS";

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public PCFProductionDeployer(JenkinsRestTemplate productionReleaseRestTemplate,
                                 ThreadSleepService threadSleepService,
                                 CIJobConfiguration ciJobConfiguration,
                                 ProductionDeployHistory productionDeployHistory) {
        this.jenkinsRestTemplate = productionReleaseRestTemplate;
        this.threadSleepService = threadSleepService;
        this.ciJobConfiguration = ciJobConfiguration;
        this.productionDeployHistory = productionDeployHistory;
    }

    public boolean push(String shaToDeploy, String storyID) {
        logger.info("Kicking off Jenkins Job to do production release");

        try {
            jenkinsRestTemplate.triggerJob(
                    ciJobConfiguration.getProductionDeployJobURL()
                            + shaToDeploy + "&STORY_ID=" + storyID);
        } catch (RuntimeException e) {
            logger.info("Call to jenkins failed, cause: ", e.getMessage());
            return false;
        }

        try {
            CIResponse jenkinsJobStatus = new CIResponse();
            do {
                logger.info("Sleeping before next poll...");
                threadSleepService.sleep(10000);
                try {
                    jenkinsJobStatus = jenkinsRestTemplate.loadJobStatus(
                            ciJobConfiguration.getProductionDeployStatusURL());
                    if (!jenkinsJobStatus.isBuilding() && jenkinsSuccessMessage.equals(jenkinsJobStatus.getResult())) {
                        logger.info("Production Deploy has finished!");
                        productionDeployHistory.push(shaToDeploy, storyID);
                        return true;
                    }
                } catch (RuntimeException e) {
                    logger.info("Call to jenkins status failed, but job kicked off, continuing polling LOL", e.getMessage());
                    jenkinsJobStatus.setBuilding(true);
                }
            } while (jenkinsJobStatus.isBuilding());
        } catch (InterruptedException e) {
            logger.info("Some thread problem", e.getMessage());
        }

        logger.error("Production deployed failed, the plane has crashed into the mountain!");
        return false;
    }
}
