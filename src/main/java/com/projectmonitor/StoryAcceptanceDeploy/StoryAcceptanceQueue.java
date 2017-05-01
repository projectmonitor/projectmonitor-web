package com.projectmonitor.StoryAcceptanceDeploy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class StoryAcceptanceQueue {

    private RedisTemplate<String, String> redisTemplate;
    public static final String STORY_ACCEPTANCE_QUEUE_NAME = "storyAcceptanceBuildQueue";
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public StoryAcceptanceQueue(RedisTemplate<String, String> template) {
        this.redisTemplate = template;
    }

    public void push(String commitSHA) {
        redisTemplate.boundListOps(STORY_ACCEPTANCE_QUEUE_NAME).rightPush(commitSHA);
    }

    public Deploy pop() {
        String queueMessage = redisTemplate.boundListOps(StoryAcceptanceQueue.STORY_ACCEPTANCE_QUEUE_NAME).leftPop();

        if (queueMessage == null || queueMessage.isEmpty()) {
            logger.info("No build messages's in queue, nothing to deploy to SA");
            return null;
        }

        String[] parts = queueMessage.split("-");
        if (parts.length != 2) {
            return null;
        }

        Deploy deploy = new Deploy();
        deploy.setSha(parts[0]);
        deploy.setStoryID(parts[1]);
        return deploy;
    }
}
