package com.projectmonitor.jenkins;

import com.projectmonitor.deploypipeline.ThreadSleepService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JenkinsJobPoller {

    static final String SUCCESS_MESSAGE = "SUCCESS";
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private ThreadSleepService threadSleepService;
    private JenkinsJobAPI jenkinsJobAPI;

    @Autowired
    public JenkinsJobPoller(ThreadSleepService threadSleepService,
                            JenkinsJobAPI jenkinsJobAPI) {
        this.threadSleepService = threadSleepService;
        this.jenkinsJobAPI = jenkinsJobAPI;
    }

    public boolean execute(String theJobToPoll) {
        try {
            CIResponse jenkinsJobStatus = new CIResponse();
            do {
                logger.info("Sleeping before next poll...");
                threadSleepService.sleep(10000);
                try {
                    jenkinsJobStatus = jenkinsJobAPI.loadJobStatus(theJobToPoll);
                    if (!jenkinsJobStatus.isBuilding() && SUCCESS_MESSAGE.equals(jenkinsJobStatus.getResult())) {
                        logger.info("Deploy has finished!");
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
        return false;
    }
}
