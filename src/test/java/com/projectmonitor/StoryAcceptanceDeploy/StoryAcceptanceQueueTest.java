package com.projectmonitor.StoryAcceptanceDeploy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StoryAcceptanceQueueTest {
    private StoryAcceptanceQueue subject;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private BoundListOperations<String, String> boundListOperations;

    @Before
    public void setUp(){
        subject = new StoryAcceptanceQueue(redisTemplate);
    }

    @Test
    public void push_addsToRedisQueueViaRedisTemplate() throws Exception {
        when(redisTemplate.boundListOps("storyAcceptanceBuildQueue")).thenReturn(boundListOperations);
        subject.push("the SHA LOL");
        verify(redisTemplate).boundListOps(StoryAcceptanceQueue.STORY_ACCEPTANCE_QUEUE_NAME);
        verify(boundListOperations).rightPush("the SHA LOL");
    }
}