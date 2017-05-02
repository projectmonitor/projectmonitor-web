package com.projectmonitor.deploypipeline;

import com.projectmonitor.jenkins.CIJobConfiguration;
import com.projectmonitor.jenkins.JenkinsJobStatus;
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

    public static final String jenkinsSuccessMessage = "SUCCESS";

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public PCFProductionDeployer(JenkinsRestTemplate productionReleaseRestTemplate,
                                 ThreadSleepService threadSleepService, CIJobConfiguration ciJobConfiguration) {
        this.jenkinsRestTemplate = productionReleaseRestTemplate;
        this.threadSleepService = threadSleepService;
        this.ciJobConfiguration = ciJobConfiguration;
    }

    public boolean push(String shaToDeploy, String storyID) {
        logger.info("Kicking off Jenkins Job to do production release");
        jenkinsRestTemplate.addAuthentication(ciJobConfiguration.getCiUsername(), ciJobConfiguration.getCiPassword());

        try {
            jenkinsRestTemplate.postForEntity(
                    ciJobConfiguration.getProductionDeployJobURL() + shaToDeploy + "&STORY_ID=" + storyID,
                    null,
                    Object.class);
        } catch (RuntimeException e) {
            logger.info("Call to jenkins failed, cause: ", e.getMessage());
            return false;
        }

        try {
            JenkinsJobStatus jenkinsJobStatus = new JenkinsJobStatus();
            do {
                logger.info("Sleeping before next poll...");
                threadSleepService.sleep(10000);
                try {
                    jenkinsJobStatus = jenkinsRestTemplate.getForObject(ciJobConfiguration.getProductionDeployStatusURL(), JenkinsJobStatus.class);
                    if (!jenkinsJobStatus.isBuilding() && jenkinsSuccessMessage.equals(jenkinsJobStatus.getResult())) {
                        logger.info("Production Deploy has finished!");
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
