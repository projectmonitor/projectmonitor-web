package com.projectmonitor.deploypipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductionDeployHistory {

    static final String KEY = "PRODUCTION_DEPLOY_QUEUE";
    private RedisTemplate<String, String> redisTemplate;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    ProductionDeployHistory(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void push(String theSHA, String theStoryID) {
        redisTemplate.boundListOps(KEY).leftPush(theSHA + "-" + theStoryID);
    }

    public Deploy getLastDeploy() {
        String queueMessage = redisTemplate.boundListOps(KEY).index(0);
        if (queueMessage == null || queueMessage.isEmpty()) {
            logger.info("No build messages's in production deploy queue");
            return null;
        }

        String[] parts = queueMessage.split("-");
        if (parts.length != 2) {
            return null;
        }

        return Deploy.builder()
                .sha(parts[0])
                .storyID(parts[1])
                .build();
    }

    public Deploy getPreviousDeploy() {
        String queueMessage = redisTemplate.boundListOps(KEY).index(1);
        if (queueMessage == null || queueMessage.isEmpty()) {
            logger.info("No build messages's in production deploy queue");
            return null;
        }

        String[] parts = queueMessage.split("-");
        if (parts.length != 2) {
            return null;
        }

        return Deploy.builder()
                .sha(parts[0])
                .storyID(parts[1])
                .build();
    }

    public void pop() {
        redisTemplate.boundListOps(KEY).leftPop();
    }
}
