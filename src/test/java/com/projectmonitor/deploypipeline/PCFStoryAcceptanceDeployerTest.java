package com.projectmonitor.deploypipeline;

import com.projectmonitor.jenkins.CIJobConfiguration;
import com.projectmonitor.jenkins.CIResponse;
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
        Deploy theDeploy = Deploy.builder().sha("theNextDeployableSHA").build();

        Mockito.when(storyAcceptanceQueue.pop()).thenReturn(theDeploy);

        when(jenkinsRestTemplate.loadJobStatus(deployStatusURL))
                .thenReturn(new CIResponse());
        subject.push();
        Mockito.verify(jenkinsRestTemplate).triggerJob(saDeployJobURL);
    }

    @Test
    public void push_whenQueueEmpty_doesNotTriggerAnSADeploy_returnsTrue() throws Exception {
        Mockito.when(storyAcceptanceQueue.pop()).thenReturn(null);
        assertThat(subject.push()).isTrue();
        Mockito.verifyZeroInteractions(jenkinsRestTemplate);
    }

    @Test
    public void push_whenSADeployJobFinishesAndIsSuccessful_returnsTrue() throws Exception {
        Deploy theDeploy = Deploy.builder().sha("theNextDeployableSHA").build();

        Mockito.when(storyAcceptanceQueue.pop()).thenReturn(theDeploy);

        CIResponse successStatus = new CIResponse();
        successStatus.setBuilding(false);
        successStatus.setResult("SUCCESS");

        CIResponse buildingStatus = new CIResponse();
        buildingStatus.setBuilding(true);
        buildingStatus.setResult("not success yet");


        when(jenkinsRestTemplate.loadJobStatus(deployStatusURL))
                .thenReturn(buildingStatus).thenReturn(successStatus);

        assertThat(subject.push()).isTrue();
        Mockito.verify(jenkinsRestTemplate, times(2)).loadJobStatus(deployStatusURL);
    }

    @Test
    public void push_whenSADeployJobFails_rejectsStoryReturnsFalse() throws Exception {
        Deploy theDeploy = Deploy.builder()
                .sha("theNextDeployableSHA")
                .storyID("theStoryID").build();

        Mockito.when(storyAcceptanceQueue.pop()).thenReturn(theDeploy);

        CIResponse failedStatus = new CIResponse();
        failedStatus.setBuilding(false);
        failedStatus.setResult("NOT A SUCCESS");
        when(jenkinsRestTemplate.loadJobStatus(deployStatusURL))
                .thenReturn(failedStatus);

        assertThat(subject.push()).isFalse();
        Mockito.verify(pivotalTrackerAPI).rejectStory("theStoryID");
    }

    @Test
    public void pushRejectedBuild_whenTheStoryPassedInMatchesTheHeadOfTheQueue_deploys_removesRejectedLabel() throws Exception {
        Deploy theDeploy = Deploy.builder()
                .sha("theNextDeployableSHA")
                .storyID("aRejectedStory").build();

        Mockito.when(storyAcceptanceQueue.readHead()).thenReturn(theDeploy);
        Mockito.when(storyAcceptanceQueue.pop()).thenReturn(theDeploy);

        when(jenkinsRestTemplate.loadJobStatus(deployStatusURL))
                .thenReturn(new CIResponse());
        subject.pushRejectedBuild("aRejectedStory");
        Mockito.verify(jenkinsRestTemplate).triggerJob(saDeployJobURL);
        Mockito.verify(pivotalTrackerAPI).removeRejectLabel("aRejectedStory");
    }

    @Test
    public void pushRejectedBuild_whenTheStoryPassedInDoesNotMatchHeadofQueue_doesNotDeploy() throws Exception {
        Deploy theDeploy = Deploy.builder()
                .sha("theNextDeployableSHA")
                .storyID("aDifferentStory").build();

        Mockito.when(storyAcceptanceQueue.readHead()).thenReturn(theDeploy);
        when(jenkinsRestTemplate.loadJobStatus(deployStatusURL))
                .thenReturn(new CIResponse());
        subject.pushRejectedBuild("aRejectedStory");
        Mockito.verify(storyAcceptanceQueue, times(0)).pop();
    }

    @Test
    public void pushRejectedBuild_whenBuildQueueIsEmpty_doesNotDeploy() throws Exception {
        subject.pushRejectedBuild("aRejectedStory");
        Mockito.verify(storyAcceptanceQueue, times(0)).pop();
    }
}