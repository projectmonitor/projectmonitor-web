package com.projectmonitor.StoryAcceptanceDeploy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import static com.projectmonitor.StoryAcceptanceDeploy.StoryAcceptanceQueue.STORY_ACCEPTANCE_QUEUE_NAME;
import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
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
        verify(redisTemplate).boundListOps(STORY_ACCEPTANCE_QUEUE_NAME);
        verify(boundListOperations).rightPush("the SHA LOL");
    }

    @Test
    public void pop_whenQueueIsEmpty_returnsNull() throws Exception {
        when(boundListOperations.leftPop()).thenReturn("");
        when(redisTemplate.boundListOps(STORY_ACCEPTANCE_QUEUE_NAME)).thenReturn(boundListOperations);
        assertThat(subject.pop()).isNull();
    }

    @Test
    public void pop_whenQueueIsPopulated_returnsDeployObjectRepresentingLeftMostQueueItem() throws Exception {
        when(boundListOperations.leftPop()).thenReturn("theSHA-theStory");
        when(redisTemplate.boundListOps(STORY_ACCEPTANCE_QUEUE_NAME)).thenReturn(boundListOperations);
        Deploy result = subject.pop();

        assertThat(result.getSha()).isEqualTo("theSHA");
        assertThat(result.getStoryID()).isEqualTo("theStory");
    }

    @Test
    public void pop_whenQueueHasGarbage_returnsNull() throws Exception {
        when(boundListOperations.leftPop()).thenReturn("theSHAOnly");
        when(redisTemplate.boundListOps(STORY_ACCEPTANCE_QUEUE_NAME)).thenReturn(boundListOperations);
        assertThat(subject.pop()).isNull();
    }
}