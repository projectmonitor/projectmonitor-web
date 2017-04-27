package com.projectmonitor.productionrelease;

import com.projectmonitor.CIJobConfiguration;
import com.projectmonitor.StoryAcceptanceDeploy.StoryAcceptanceQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PCFStoryAcceptanceDeployer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private final RedisTemplate<String, String> redisTemplate;
    private final RestTemplate productionReleaseRestTemplate;
    private final CIJobConfiguration ciJobConfiguration;

    @Autowired
    public PCFStoryAcceptanceDeployer(RestTemplate productionReleaseRestTemplate,
                                      RedisTemplate<String, String> redisTemplate,
                                      CIJobConfiguration ciJobConfiguration) {
        this.productionReleaseRestTemplate = productionReleaseRestTemplate;
        this.redisTemplate = redisTemplate;
        this.ciJobConfiguration = ciJobConfiguration;
    }

    public void push() {
        String theSHA = redisTemplate.boundListOps(StoryAcceptanceQueue.STORY_ACCEPTANCE_QUEUE_NAME).leftPop();

        if (theSHA == null || theSHA.isEmpty()) {
            logger.info("No build SHA's in queue, nothing to deploy to SA\n");
            return;
        }

        logger.info("Deploying to Story Acceptance with the following SHA: " + theSHA);
        productionReleaseRestTemplate.getForObject(ciJobConfiguration.getStoryAcceptanceDeployJobURL() + theSHA, Object.class);
    }
}
