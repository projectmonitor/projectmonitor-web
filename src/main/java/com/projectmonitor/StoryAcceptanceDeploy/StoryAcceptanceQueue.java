package com.projectmonitor.StoryAcceptanceDeploy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class StoryAcceptanceQueue {
    private RedisTemplate<String, String> redisTemplate;
    public static final String STORY_ACCEPTANCE_QUEUE_NAME = "storyAcceptanceBuildQueue";

    @Autowired
    public StoryAcceptanceQueue(RedisTemplate<String, String> template) {
        this.redisTemplate = template;
    }

    public void push(String commitSHA) {
        redisTemplate.boundListOps(STORY_ACCEPTANCE_QUEUE_NAME).rightPush(commitSHA);
    }
}
