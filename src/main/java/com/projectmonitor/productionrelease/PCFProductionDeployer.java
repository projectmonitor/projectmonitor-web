package com.projectmonitor.productionrelease;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PCFProductionDeployer {

    private final RestTemplate productionReleaseRestTemplate;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public PCFProductionDeployer(RestTemplate productionReleaseRestTemplate) {
        this.productionReleaseRestTemplate = productionReleaseRestTemplate;
    }

    public void push(String shaToDeploy) {
        logger.info("Kicking off Jenkins Job to do production release");

        try {
            String productionDeployURL = "http://localhost:8080/job/TestProject to Production/buildWithParameters?SHA_TO_DEPLOY=";
            productionReleaseRestTemplate.getForObject(
                    productionDeployURL + shaToDeploy,
                    Object.class);
        } catch (RuntimeException e) {
            logger.info("Call to jenkins failed, cause: ", e.getMessage());
            return;
        }
    }
}
