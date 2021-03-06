package com.projectmonitor.deployjob;

import com.projectmonitor.deploys.Deploy;
import com.projectmonitor.deploys.StoryAcceptanceQueue;
import com.projectmonitor.jenkins.CIJobConfiguration;
import com.projectmonitor.jenkins.CIResponse;
import com.projectmonitor.jenkins.JenkinsJobPoller;
import com.projectmonitor.jenkins.JenkinsClient;
import com.projectmonitor.pivotaltracker.PivotalTrackerAPI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PCFStoryAcceptanceDeployerTest {

    private PCFStoryAcceptanceDeployer subject;
    @Mock
    private JenkinsClient jenkinsClient;
    @Mock
    private StoryAcceptanceQueue storyAcceptanceQueue;
    @Mock
    private PivotalTrackerAPI pivotalTrackerAPI;
    @Mock
    private JenkinsJobPoller jenkinsJobPoller;

    private String saDeployJobURL = "http://localhost:8080/job/TestProject to SA/buildWithParameters?ShaToBuild=theNextDeployableSHA";
    private String deployStatusURL = "http://localhost:8080/job/TestProject to SA/lastBuild/api/json";

    @Before
    public void setUp() {
        CIJobConfiguration ciJobConfiguration = new CIJobConfiguration();
        ciJobConfiguration.setStoryAcceptanceDeployJobURL("http://localhost:8080/job/TestProject to SA/buildWithParameters?ShaToBuild=");
        ciJobConfiguration.setStoryAcceptanceDeployStatusURL(deployStatusURL);
        subject = new PCFStoryAcceptanceDeployer(storyAcceptanceQueue, jenkinsClient, ciJobConfiguration, pivotalTrackerAPI, jenkinsJobPoller);
    }

    @Test
    public void push_triggersTheJenkinsSADeployJob_withTheNextBuildThatHasPassedCI() throws Exception {
        Deploy theDeploy = Deploy.builder().sha("theNextDeployableSHA").build();

        Mockito.when(storyAcceptanceQueue.readHead()).thenReturn(theDeploy);

        when(jenkinsClient.loadJobStatus(deployStatusURL))
                .thenReturn(new CIResponse());
        subject.push();
        Mockito.verify(jenkinsClient).triggerJob(saDeployJobURL);
    }

    @Test
    public void push_whenQueueEmpty_doesNotTriggerAnSADeploy_returnsTrue() throws Exception {
        Mockito.when(storyAcceptanceQueue.readHead()).thenReturn(null);
        assertThat(subject.push()).isTrue();
        Mockito.verifyZeroInteractions(jenkinsClient);
    }

    @Test
    public void push_whenSADeployJobFinishesAndIsSuccessful_returnsTrueAndPopBuildOutOfQueue() throws Exception {
        Deploy theDeploy = Deploy.builder().sha("theNextDeployableSHA").build();

        Mockito.when(storyAcceptanceQueue.readHead()).thenReturn(theDeploy);

        when(jenkinsJobPoller.execute(deployStatusURL)).thenReturn(true);

        assertThat(subject.push()).isTrue();
        verify(storyAcceptanceQueue).pop();
    }

    @Test
    public void push_whenSADeployJobFails_rejectsStoryReturnsFalse() throws Exception {
        Deploy theDeploy = Deploy.builder()
                .sha("theNextDeployableSHA")
                .storyID("theStoryID").build();

        Mockito.when(storyAcceptanceQueue.pop()).thenReturn(theDeploy);

        when(jenkinsJobPoller.execute(deployStatusURL)).thenReturn(false);

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

        when(jenkinsClient.loadJobStatus(deployStatusURL))
                .thenReturn(new CIResponse());
        subject.pushRejectedBuild("aRejectedStory");
        Mockito.verify(jenkinsClient).triggerJob(saDeployJobURL);
        Mockito.verify(pivotalTrackerAPI).removeRejectLabel("aRejectedStory");
    }

    @Test
    public void pushRejectedBuild_whenTheStoryPassedInDoesNotMatchHeadofQueue_doesNotDeploy() throws Exception {
        Deploy theDeploy = Deploy.builder()
                .sha("theNextDeployableSHA")
                .storyID("aDifferentStory").build();

        Mockito.when(storyAcceptanceQueue.readHead()).thenReturn(theDeploy);
        when(jenkinsClient.loadJobStatus(deployStatusURL))
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