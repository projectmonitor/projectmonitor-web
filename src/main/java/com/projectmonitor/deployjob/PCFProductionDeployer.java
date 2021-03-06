package com.projectmonitor.deployjob;

import com.projectmonitor.deploys.ProductionDeployHistory;
import com.projectmonitor.jenkins.CIJobConfiguration;
import com.projectmonitor.jenkins.JenkinsJobPoller;
import com.projectmonitor.jenkins.JenkinsClient;
import com.projectmonitor.jenkins.RequestFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class PCFProductionDeployer {

    private JenkinsClient jenkinsClient;
    private CIJobConfiguration ciJobConfiguration;
    private final ProductionDeployHistory productionDeployHistory;
    private JenkinsJobPoller jenkinsJobPoller;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public PCFProductionDeployer(JenkinsClient jenkinsClient,
                                 CIJobConfiguration ciJobConfiguration,
                                 ProductionDeployHistory productionDeployHistory,
                                 JenkinsJobPoller jenkinsJobPoller) {
        this.jenkinsClient = jenkinsClient;
        this.ciJobConfiguration = ciJobConfiguration;
        this.productionDeployHistory = productionDeployHistory;
        this.jenkinsJobPoller = jenkinsJobPoller;
    }

    public boolean push(String shaToDeploy, String storyID) {
        logger.info("Kicking off Jenkins Job to do production release");

        try {
            jenkinsClient.triggerJob(
                    ciJobConfiguration.getProductionDeployJobURL()
                            + shaToDeploy + "&STORY_ID=" + storyID);
        } catch (RuntimeException | RequestFailedException e) {
            logger.info("Call to jenkins failed, cause: {}", e.getMessage(), e);
            return false;
        }

        if (jenkinsJobPoller.execute(ciJobConfiguration.getProductionDeployStatusURL())) {
            logger.info("Production Deploy has finished!");
            productionDeployHistory.push(shaToDeploy, storyID);
            return true;
        }

        logger.error("Production deployed failed, the plane has crashed into the mountain!");
        return false;
    }
}
