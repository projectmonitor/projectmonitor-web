package com.projectmonitor.productionrelease;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Component
public class PCFProductionDeployer {

    private final RestTemplate productionReleaseRestTemplate;
    private final ThreadSleepService threadSleepService;
    private final String prodDeployStatusURL = "http://localhost:8080/job/TestProject to Production/lastBuild/api/json";
    private final String productionDeployURL = "http://localhost:8080/job/TestProject to Production/buildWithParameters?SHA_TO_DEPLOY=";
    public static final String jenkinsSuccessMessage = "SUCCESS";
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public PCFProductionDeployer(RestTemplate productionReleaseRestTemplate,
                                 ThreadSleepService threadSleepService) {
        this.productionReleaseRestTemplate = productionReleaseRestTemplate;
        this.threadSleepService = threadSleepService;
    }

    public boolean push(String shaToDeploy) {
        logger.info("Kicking off Jenkins Job to do production release");

        try {
            productionReleaseRestTemplate.getForObject(
                    productionDeployURL + shaToDeploy,
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

                    jenkinsJobStatus = productionReleaseRestTemplate.getForObject(prodDeployStatusURL, JenkinsJobStatus.class);
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
