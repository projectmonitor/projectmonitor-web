package com.projectmonitor.deploypipeline;

import com.projectmonitor.pivotaltracker.PivotalTrackerAPIService;
import com.projectmonitor.pivotaltracker.PivotalTrackerStory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class StoryAcceptanceQueue {

    private RedisTemplate<String, String> redisTemplate;
    private PivotalTrackerAPIService pivotalTrackerAPIService;
    static final String STORY_ACCEPTANCE_QUEUE_NAME = "storyAcceptanceBuildQueue";
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public StoryAcceptanceQueue(RedisTemplate<String, String> template, PivotalTrackerAPIService pivotalTrackerAPIService) {
        this.redisTemplate = template;
        this.pivotalTrackerAPIService = pivotalTrackerAPIService;
    }

    public void push(String commitSHA, String storyID) {
        PivotalTrackerStory theStory = pivotalTrackerAPIService.getStory(storyID);
        if (theStory.isHasBeenRejected()) {
            redisTemplate.boundListOps(STORY_ACCEPTANCE_QUEUE_NAME).leftPush(commitSHA + "-" + storyID);
            return;
        }

        redisTemplate.boundListOps(STORY_ACCEPTANCE_QUEUE_NAME).rightPush(commitSHA + "-" + storyID);
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

    public Deploy readHead() {
        String queueMessage = redisTemplate.boundListOps(StoryAcceptanceQueue.STORY_ACCEPTANCE_QUEUE_NAME).index(0);
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
