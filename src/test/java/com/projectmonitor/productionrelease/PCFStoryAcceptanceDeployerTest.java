package com.projectmonitor.productionrelease;

import com.projectmonitor.CIJobConfiguration;
import com.projectmonitor.StoryAcceptanceDeploy.StoryAcceptanceQueue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;

@RunWith(MockitoJUnitRunner.class)
public class PCFStoryAcceptanceDeployerTest {

    PCFStoryAcceptanceDeployer subject;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private BoundListOperations<String, String> boundListOperations;

    @Mock
    private RestTemplate productionReleaseRestTemplate;

    CIJobConfiguration ciJobConfiguration;

    @Before
    public void setUp(){
        ciJobConfiguration = new CIJobConfiguration();
        ciJobConfiguration.setStoryAcceptanceDeployJobURL("http://localhost:8080/job/TestProject to SA/buildWithParameters?ShaToBuild=");
        subject = new PCFStoryAcceptanceDeployer(productionReleaseRestTemplate, redisTemplate, ciJobConfiguration);
    }

    @Test
    public void push_triggersTheJenkinsSADeployJob_withTheNextBuildThatHasPassedCI() throws Exception {
        Mockito.when(boundListOperations.leftPop()).thenReturn("theNextDeployableSHA");
        Mockito.when(redisTemplate.boundListOps(StoryAcceptanceQueue.STORY_ACCEPTANCE_QUEUE_NAME)).thenReturn(boundListOperations);
        subject.push();
        Mockito.verify(productionReleaseRestTemplate)
                .getForObject("http://localhost:8080/job/TestProject to SA/buildWithParameters?ShaToBuild=theNextDeployableSHA", Object.class);
    }

    @Test
    public void push_whenQueueEmpty_doesNotTriggerAnSADeploy() throws Exception {
        Mockito.when(boundListOperations.leftPop()).thenReturn(null);
        Mockito.when(redisTemplate.boundListOps(StoryAcceptanceQueue.STORY_ACCEPTANCE_QUEUE_NAME)).thenReturn(boundListOperations);
        subject.push();
        Mockito.verifyZeroInteractions(productionReleaseRestTemplate);
    }
}