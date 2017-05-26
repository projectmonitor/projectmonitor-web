package com.projectmonitor.deploys;

import com.projectmonitor.pivotaltracker.PivotalTrackerAPI;
import com.projectmonitor.pivotaltracker.PivotalTrackerStory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import static com.projectmonitor.deploys.StoryAcceptanceQueue.STORY_ACCEPTANCE_QUEUE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StoryAcceptanceQueueTest {

    private StoryAcceptanceQueue subject;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private BoundListOperations<String, String> boundListOperations;
    @Mock
    private PivotalTrackerAPI pivotalTrackerAPI;

    @Before
    public void setUp() {
        subject = new StoryAcceptanceQueue(redisTemplate, pivotalTrackerAPI);
    }

    @Test
    public void push_addsToRedisQueueViaRedisTemplate() throws Exception {
        when(redisTemplate.boundListOps("storyAcceptanceBuildQueue")).thenReturn(boundListOperations);
        when(pivotalTrackerAPI.getStory("theStoryID")).thenReturn(PivotalTrackerStory.builder().build());
        subject.push("theSHALOL", "theStoryID");
        verify(redisTemplate).boundListOps(STORY_ACCEPTANCE_QUEUE_NAME);
        verify(boundListOperations).rightPush("theSHALOL-theStoryID");
    }

    @Test
    public void push_whenTheStoryHasBeenRejected_addsBuildToHeadOfQueue() throws Exception {
        PivotalTrackerStory rejectedStory = PivotalTrackerStory.builder()
                .currentState("whatever").hasBeenRejected(true).build();

        when(pivotalTrackerAPI.getStory("rejectedStory")).thenReturn(rejectedStory);

        when(redisTemplate.boundListOps("storyAcceptanceBuildQueue")).thenReturn(boundListOperations);
        subject.push("veryGoodCommit", "rejectedStory");
        verify(boundListOperations).leftPush("veryGoodCommit-rejectedStory");
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

    @Test
    public void readHead_whenQueueHasData_grabsHeadOfListFromRedisTemplate() throws Exception {
        when(redisTemplate.boundListOps("storyAcceptanceBuildQueue")).thenReturn(boundListOperations);
        when(boundListOperations.index(0)).thenReturn("theSHALOL-theStoryID");
        Deploy result = subject.readHead();
        verify(redisTemplate).boundListOps(STORY_ACCEPTANCE_QUEUE_NAME);
        verify(boundListOperations).index(0);
        assertThat(result.getSha()).isEqualTo("theSHALOL");
        assertThat(result.getStoryID()).isEqualTo("theStoryID");
    }

    @Test
    public void readHead_whenQueueIsEmpty_returnsNull() throws Exception {
        when(redisTemplate.boundListOps("storyAcceptanceBuildQueue")).thenReturn(boundListOperations);
        when(boundListOperations.index(0)).thenReturn(null);
        Deploy result = subject.readHead();
        assertThat(result).isNull();
    }
}