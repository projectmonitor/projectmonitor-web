package com.projectmonitor.deploypipeline;

import com.projectmonitor.jenkins.CIJobConfiguration;
import com.projectmonitor.jenkins.JenkinsJobStatus;
import com.projectmonitor.jenkins.JenkinsRestTemplate;
import com.projectmonitor.pivotaltracker.PivotalTrackerAPI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PCFStoryAcceptanceDeployerTest {

    private PCFStoryAcceptanceDeployer subject;
    @Mock
    private JenkinsRestTemplate jenkinsRestTemplate;
    @Mock
    private ThreadSleepService threadSleepService;
    @Mock
    private StoryAcceptanceQueue storyAcceptanceQueue;
    @Mock
    private PivotalTrackerAPI pivotalTrackerAPI;

    private CIJobConfiguration ciJobConfiguration;
    private String saDeployJobURL = "http://localhost:8080/job/TestProject to SA/buildWithParameters?ShaToBuild=theNextDeployableSHA";
    private String deployStatusURL = "http://localhost:8080/job/TestProject to SA/lastBuild/api/json";

    @Before
    public void setUp() {
        ciJobConfiguration = new CIJobConfiguration();
        ciJobConfiguration.setStoryAcceptanceDeployJobURL("http://localhost:8080/job/TestProject to SA/buildWithParameters?ShaToBuild=");
        ciJobConfiguration.setStoryAcceptanceDeployStatusURL(deployStatusURL);
        subject = new PCFStoryAcceptanceDeployer(storyAcceptanceQueue, jenkinsRestTemplate, ciJobConfiguration, threadSleepService, pivotalTrackerAPI);
    }

    @Test
    public void push_triggersTheJenkinsSADeployJob_withTheNextBuildThatHasPassedCI() throws Exception {
        Deploy theDeploy = new Deploy();
        theDeploy.setSha("theNextDeployableSHA");

        Mockito.when(storyAcceptanceQueue.pop()).thenReturn(theDeploy);

        when(jenkinsRestTemplate.getForObject(deployStatusURL, JenkinsJobStatus.class))
                .thenReturn(new JenkinsJobStatus());
        subject.push();
        Mockito.verify(jenkinsRestTemplate).postForEntity(saDeployJobURL, null, String.class);
    }

    @Test
    public void push_whenQueueEmpty_doesNotTriggerAnSADeploy_returnsTrue() throws Exception {
        Mockito.when(storyAcceptanceQueue.pop()).thenReturn(null);
        assertThat(subject.push()).isTrue();
        Mockito.verifyZeroInteractions(jenkinsRestTemplate);
    }

    @Test
    public void push_whenSADeployJobFinishesAndIsSuccessful_returnsTrue() throws Exception {
        Deploy theDeploy = new Deploy();
        theDeploy.setSha("theNextDeployableSHA");

        Mockito.when(storyAcceptanceQueue.pop()).thenReturn(theDeploy);

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
    public void push_whenSADeployJobFails_rejectsStoryReturnsFalse() throws Exception {
        Deploy theDeploy = new Deploy();
        theDeploy.setSha("theNextDeployableSHA");
        theDeploy.setStoryID("theStoryID");

        Mockito.when(storyAcceptanceQueue.pop()).thenReturn(theDeploy);

        JenkinsJobStatus failedStatus = new JenkinsJobStatus();
        failedStatus.setBuilding(false);
        failedStatus.setResult("NOT A SUCCESS");
        when(jenkinsRestTemplate.getForObject(deployStatusURL, JenkinsJobStatus.class))
                .thenReturn(failedStatus);

        assertThat(subject.push()).isFalse();
        Mockito.verify(pivotalTrackerAPI).rejectStory("theStoryID");
    }

    @Test
    public void pushRejectedBuild_whenTheStoryPassedInMatchesTheHeadOfTheQueue_deploys_removesRejectedLabel() throws Exception {
        Deploy theDeploy = new Deploy();
        theDeploy.setSha("theNextDeployableSHA");
        theDeploy.setStoryID("aRejectedStory");

        Mockito.when(storyAcceptanceQueue.readHead()).thenReturn(theDeploy);
        Mockito.when(storyAcceptanceQueue.pop()).thenReturn(theDeploy);

        when(jenkinsRestTemplate.getForObject(deployStatusURL, JenkinsJobStatus.class))
                .thenReturn(new JenkinsJobStatus());
        subject.pushRejectedBuild("aRejectedStory");
        Mockito.verify(jenkinsRestTemplate).postForEntity(saDeployJobURL, null, String.class);
        Mockito.verify(pivotalTrackerAPI).removeRejectLabel("aRejectedStory");
    }

    @Test
    public void pushRejectedBuild_whenTheStoryPassedInDoesNotMatchHeadofQueue_doesNotDeploy() throws Exception {
        Deploy theDeploy = new Deploy();
        theDeploy.setSha("theNextDeployableSHA");
        theDeploy.setStoryID("aDifferentStory");

        Mockito.when(storyAcceptanceQueue.readHead()).thenReturn(theDeploy);
        when(jenkinsRestTemplate.getForObject(deployStatusURL, JenkinsJobStatus.class))
                .thenReturn(new JenkinsJobStatus());
        subject.pushRejectedBuild("aRejectedStory");
        Mockito.verify(storyAcceptanceQueue, times(0)).pop();
    }
}