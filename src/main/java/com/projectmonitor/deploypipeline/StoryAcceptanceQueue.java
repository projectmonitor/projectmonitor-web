package com.projectmonitor.deploypipeline;

import com.projectmonitor.pivotaltracker.PivotalTrackerAPI;
import com.projectmonitor.pivotaltracker.PivotalTrackerStory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class StoryAcceptanceQueue {

    private RedisTemplate<String, String> redisTemplate;
    private PivotalTrackerAPI pivotalTrackerAPI;
    static final String STORY_ACCEPTANCE_QUEUE_NAME = "storyAcceptanceBuildQueue";
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public StoryAcceptanceQueue(RedisTemplate<String, String> template,
                                PivotalTrackerAPI pivotalTrackerAPI) {
        this.redisTemplate = template;
        this.pivotalTrackerAPI = pivotalTrackerAPI;
    }

    public void push(String commitSHA, String storyID) {
        PivotalTrackerStory theStory = pivotalTrackerAPI.getStory(storyID);
        if (theStory.isHasBeenRejected()) {
            redisTemplate.boundListOps(STORY_ACCEPTANCE_QUEUE_NAME).leftPush(commitSHA + "-" + storyID);
            return;
        }

        redisTemplate.boundListOps(STORY_ACCEPTANCE_QUEUE_NAME).rightPush(commitSHA + "-" + storyID);
    }

    Deploy pop() {
        String queueMessage = redisTemplate.boundListOps(StoryAcceptanceQueue.STORY_ACCEPTANCE_QUEUE_NAME).leftPop();

        if (queueMessage == null || queueMessage.isEmpty()) {
            logger.info("No build messages's in queue, nothing to deploy to SA");
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

    Deploy readHead() {
        String queueMessage = redisTemplate.boundListOps(StoryAcceptanceQueue.STORY_ACCEPTANCE_QUEUE_NAME).index(0);

        if (queueMessage == null || queueMessage.isEmpty()) {
            logger.info("No build messages's in queue, nothing to deploy to SA");
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
}
