package com.projectmonitor.productionrelease;

import com.projectmonitor.CIJobConfiguration;
import com.projectmonitor.StoryAcceptanceDeploy.StoryAcceptanceQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class PCFStoryAcceptanceDeployer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private final RedisTemplate<String, String> redisTemplate;
    private final JenkinsRestTemplate restTemplate;
    private final CIJobConfiguration ciJobConfiguration;
    private final ThreadSleepService threadSleepService;
    public static final String JENKINS_SUCCESS_MESSAGE = "SUCCESS";

    @Autowired
    public PCFStoryAcceptanceDeployer(RedisTemplate<String, String> redisTemplate,
                                      JenkinsRestTemplate restTemplate,
                                      CIJobConfiguration ciJobConfiguration,
                                      ThreadSleepService threadSleepService) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
        this.ciJobConfiguration = ciJobConfiguration;
        this.threadSleepService = threadSleepService;
    }

    public boolean push() {
        String theSHA = redisTemplate.boundListOps(StoryAcceptanceQueue.STORY_ACCEPTANCE_QUEUE_NAME).leftPop();

        if (theSHA == null || theSHA.isEmpty()) {
            logger.info("No build SHA's in queue, nothing to deploy to SA\n");
            return true;
        }

        logger.info("Deploying to Story Acceptance with the following SHA: " + theSHA);
        restTemplate.addAuthentication(ciJobConfiguration.getCiUsername(), ciJobConfiguration.getCiPassword());

        try {
            restTemplate.postForEntity(ciJobConfiguration.getStoryAcceptanceDeployJobURL() + theSHA,
                    null,
                    String.class);
        } catch (RuntimeException e) {
            logger.info("Call to kickoff story acceptance deploy failed, cause: ", e.getMessage());
            return false;
        }

        try {
            JenkinsJobStatus jenkinsJobStatus = new JenkinsJobStatus();
            do {
                logger.info("Sleeping before next poll...");
                threadSleepService.sleep(15000);
                try {
                    jenkinsJobStatus = restTemplate.getForObject(ciJobConfiguration.getStoryAcceptanceDeployStatusURL(), JenkinsJobStatus.class);
                    if (!jenkinsJobStatus.isBuilding() && JENKINS_SUCCESS_MESSAGE.equals(jenkinsJobStatus.getResult())) {
                        logger.info("Story Acceptance Deploy has finished!");
                        return true;
                    }
                } catch (RuntimeException e) {
                    logger.info("Call to story acceptance deploy status failed, but job kicked off, continuing polling...", e.getMessage());
                    jenkinsJobStatus.setBuilding(true);
                }
            } while (jenkinsJobStatus.isBuilding());
        } catch (InterruptedException e) {
            logger.info("Some thread problem", e.getMessage());
        }

        return false;
    }
}
