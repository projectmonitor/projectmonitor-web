package com.projectmonitor.productionrelease;

import com.projectmonitor.CIJobConfiguration;
import com.projectmonitor.StoryAcceptanceDeploy.StoryAcceptanceQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class PCFStoryAcceptanceDeployer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private final RedisTemplate<String, String> redisTemplate;
    private final JenkinsRestTemplate restTemplate;
    private final CIJobConfiguration ciJobConfiguration;

    @Autowired
    public PCFStoryAcceptanceDeployer(RedisTemplate<String, String> redisTemplate,
                                      JenkinsRestTemplate restTemplate,
                                      CIJobConfiguration ciJobConfiguration) {
        this.restTemplate = restTemplate;
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
        restTemplate.addAuthentication(ciJobConfiguration.getCiUsername(), ciJobConfiguration.getCiPassword());
        restTemplate.postForEntity(ciJobConfiguration.getStoryAcceptanceDeployJobURL() + theSHA,
                null, String.class);
    }
}
