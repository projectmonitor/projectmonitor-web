package com.projectmonitor.productionrelease;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PCFDeployer {

    private final RestTemplate productionReleaseRestTemplate;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public PCFDeployer(RestTemplate productionReleaseRestTemplate) {
        this.productionReleaseRestTemplate = productionReleaseRestTemplate;
    }

    public void push() {
        logger.info("Kicking off Jenkins Job to do production release");

        try {
            productionReleaseRestTemplate.getForObject(
                    "http://localhost:9000/job/TestProject to Production/build?token=TestProjectToProduction",
                    Object.class);
        } catch (RuntimeException e) {
            logger.info("Call to jenkins failed, cause: ", e.getMessage());
            return;
        }
    }
}
