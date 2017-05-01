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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PCFStoryAcceptanceDeployerTest {

    PCFStoryAcceptanceDeployer subject;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private BoundListOperations<String, String> boundListOperations;
    @Mock
    private JenkinsRestTemplate jenkinsRestTemplate;
    @Mock
    private ThreadSleepService threadSleepService;

    private CIJobConfiguration ciJobConfiguration;
    private String saDeployJobURL = "http://localhost:8080/job/TestProject to SA/buildWithParameters?ShaToBuild=theNextDeployableSHA";
    private String deployStatusURL = "http://localhost:8080/job/TestProject to SA/lastBuild/api/json";

    @Before
    public void setUp() {
        ciJobConfiguration = new CIJobConfiguration();
        ciJobConfiguration.setStoryAcceptanceDeployJobURL("http://localhost:8080/job/TestProject to SA/buildWithParameters?ShaToBuild=");
        ciJobConfiguration.setStoryAcceptanceDeployStatusURL(deployStatusURL);
        subject = new PCFStoryAcceptanceDeployer(redisTemplate, jenkinsRestTemplate, ciJobConfiguration, threadSleepService);
    }

    @Test
    public void push_triggersTheJenkinsSADeployJob_withTheNextBuildThatHasPassedCI() throws Exception {

        Mockito.when(boundListOperations.leftPop()).thenReturn("theNextDeployableSHA");
        Mockito.when(redisTemplate.boundListOps(StoryAcceptanceQueue.STORY_ACCEPTANCE_QUEUE_NAME)).thenReturn(boundListOperations);
        when(jenkinsRestTemplate.getForObject(deployStatusURL, JenkinsJobStatus.class))
                .thenReturn(new JenkinsJobStatus());
        subject.push();
        Mockito.verify(jenkinsRestTemplate).postForEntity(saDeployJobURL, null, String.class);
    }

    @Test
    public void push_whenQueueEmpty_doesNotTriggerAnSADeploy_returnsTrue() throws Exception {
        Mockito.when(boundListOperations.leftPop()).thenReturn(null);
        Mockito.when(redisTemplate.boundListOps(StoryAcceptanceQueue.STORY_ACCEPTANCE_QUEUE_NAME)).thenReturn(boundListOperations);
        assertThat(subject.push()).isTrue();
        Mockito.verifyZeroInteractions(jenkinsRestTemplate);
    }

    @Test
    public void push_whenSADeployJobFinishesAndIsSuccessful_returnsTrue() throws Exception {
        Mockito.when(boundListOperations.leftPop()).thenReturn("theNextDeployableSHA");
        Mockito.when(redisTemplate.boundListOps(StoryAcceptanceQueue.STORY_ACCEPTANCE_QUEUE_NAME)).thenReturn(boundListOperations);

        JenkinsJobStatus successStatus = new JenkinsJobStatus();
        successStatus.setBuilding(false);
        successStatus.setResult("SUCCESS");

        JenkinsJobStatus buildingStatus = new JenkinsJobStatus();
        buildingStatus.setBuilding(true);
        buildingStatus.setResult("not success yet");

        when(jenkinsRestTemplate.postForEntity(saDeployJobURL, null, Object.class)).thenReturn(null);
        when(jenkinsRestTemplate.getForObject(deployStatusURL, JenkinsJobStatus.class))
                .thenReturn(buildingStatus).thenReturn(successStatus);

        assertThat(subject.push()).isTrue();
        Mockito.verify(jenkinsRestTemplate, times(2)).getForObject(deployStatusURL, JenkinsJobStatus.class);
    }

    @Test
    public void push_whenSADeployJobFails_returnsFalse() throws Exception {
        Mockito.when(boundListOperations.leftPop()).thenReturn("theNextDeployableSHA");
        Mockito.when(redisTemplate.boundListOps(StoryAcceptanceQueue.STORY_ACCEPTANCE_QUEUE_NAME)).thenReturn(boundListOperations);

        JenkinsJobStatus failedStatus = new JenkinsJobStatus();
        failedStatus.setBuilding(false);
        failedStatus.setResult("NOT A SUCCESS");
        when(jenkinsRestTemplate.getForObject(deployStatusURL, JenkinsJobStatus.class))
                .thenReturn(failedStatus);

        assertThat(subject.push()).isFalse();
    }
}