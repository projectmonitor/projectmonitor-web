package com.projectmonitor.jenkins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JenkinsJobPoller {

    static final String SUCCESS_MESSAGE = "SUCCESS";
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private ThreadSleepService threadSleepService;
    private JenkinsClient jenkinsClient;

    @Autowired
    public JenkinsJobPoller(ThreadSleepService threadSleepService,
                            JenkinsClient jenkinsClient) {
        this.threadSleepService = threadSleepService;
        this.jenkinsClient = jenkinsClient;
    }

    public boolean execute(String theJobToPoll) {
        try {
            CIResponse jenkinsJobStatus = new CIResponse();
            do {
                logger.info("Sleeping before next poll...");
                threadSleepService.sleep(10000);
                try {
                    jenkinsJobStatus = jenkinsClient.loadJobStatus(theJobToPoll);
                    if (!jenkinsJobStatus.isBuilding() && SUCCESS_MESSAGE.equals(jenkinsJobStatus.getResult())) {
                        logger.info("Deploy has finished!");
                        return true;
                    }
                } catch (RuntimeException | RequestFailedException e) {
                    logger.info("Call to jenkins status failed, but job kicked off, continuing polling LOL", e.getMessage());
                    jenkinsJobStatus.setBuilding(true);
                }
            } while (jenkinsJobStatus.isBuilding());
        } catch (InterruptedException e) {
            logger.info("Some thread problem", e.getMessage());
        }
        return false;
    }
}
