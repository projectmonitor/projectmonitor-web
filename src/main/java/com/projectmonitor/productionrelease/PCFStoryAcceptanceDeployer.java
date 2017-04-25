package com.projectmonitor.productionrelease;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PCFStoryAcceptanceDeployer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public void push() {
        logger.info("Determining what to attempt to deploy to Story Acceptance");

    }
}
