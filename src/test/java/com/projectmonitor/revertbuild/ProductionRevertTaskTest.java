package com.projectmonitor.revertbuild;

import com.projectmonitor.deploys.Deploy;
import com.projectmonitor.deploys.ProductionDeployHistory;
import com.projectmonitor.deploys.StoryAcceptanceQueue;
import com.projectmonitor.environments.Environments;
import com.projectmonitor.jenkins.JenkinsAPI;
import com.projectmonitor.jenkins.RevertFailedException;
import com.projectmonitor.pivotaltracker.PivotalTrackerAPI;
import com.projectmonitor.environments.DeployedAppInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductionRevertTaskTest {

    @InjectMocks
    private ProductionRevertTask subject;

    @Mock
    private ProductionDeployHistory productionDeployHistory;

    @Mock
    private StoryAcceptanceQueue storyAcceptanceQueue;

    @Mock
    private Environments environments;

    @Mock
    private PivotalTrackerAPI pivotalTrackerAPI;

    @Mock
    private JenkinsAPI jenkinsAPI;

    @Mock
    private ProductionRevertFlag productionRevertFlag;

    @Mock
    private RevertErrorRepository revertErrorRepository;

    private Deploy previousDeploy;

    @Before
    public void setUp() {
        when(productionDeployHistory.getLastDeploy())
                .thenReturn(Deploy.builder().build());

        previousDeploy = Deploy.builder().build();
        when(productionDeployHistory.getPreviousDeploy())
                .thenReturn(previousDeploy);

        when(environments.loadStoryAcceptanceDeployedAppInfo())
                .thenReturn(new DeployedAppInfo());
    }

    @Test
    public void start_determinesProductionVersionFromQueue() throws Exception {
        subject.start();

        verify(productionDeployHistory).getLastDeploy();
    }

    @Test
    public void start_determinesStoryAcceptanceBuild() throws Exception {
        subject.start();

        verify(environments).loadStoryAcceptanceDeployedAppInfo();
    }

    @Test
    public void start_determinesPreviousProductionBuild() throws Exception {
        subject.start();

        verify(productionDeployHistory).getPreviousDeploy();
    }

    @Test
    public void whenThereIsntAPreviousProductionDeploy_exits() throws Exception {
        when(productionDeployHistory.getPreviousDeploy())
                .thenReturn(null);
        subject.start();

        verifyZeroInteractions(pivotalTrackerAPI);
    }

    @Test
    public void start_rejectsTheStoryRelatedToCurrentProductionDeploy() throws Exception {
        when(productionDeployHistory.getLastDeploy())
                .thenReturn(Deploy.builder().storyID("the-story-being-reverted").build());

        subject.start();

        verify(pivotalTrackerAPI).rejectStory("the-story-being-reverted");
    }

    @Test
    public void whenRejectingTheStoryFails_itStillRevertsProduction() throws Exception {
        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST)).when(pivotalTrackerAPI).rejectStory(any());
        subject.start();
        verify(jenkinsAPI).revertProduction(any());
    }

    @Test
    public void start_triggersProdDeployment() throws Exception {
        Deploy previousProductionDeploy = Deploy.builder()
                .sha("previous-sha")
                .storyID("previous-prod-story").build();

        when(productionDeployHistory.getPreviousDeploy())
                .thenReturn(previousProductionDeploy);

        subject.start();

        verify(jenkinsAPI).revertProduction(previousProductionDeploy);
    }

    @Test
    public void start_marksAcceptanceStoryAsFinished() throws Exception {
        DeployedAppInfo storyAcceptanceInfo = new DeployedAppInfo();
        storyAcceptanceInfo.setPivotalTrackerStoryID("the-story-being-finished");
        when(environments.loadStoryAcceptanceDeployedAppInfo())
                .thenReturn(storyAcceptanceInfo);

        subject.start();

        verify(pivotalTrackerAPI).finishStory("the-story-being-finished");
    }

    @Test
    public void start_putStoryAcceptanceStoryInFrontOfQueue() throws Exception {
        DeployedAppInfo storyAcceptanceInfo = new DeployedAppInfo();
        storyAcceptanceInfo.setPivotalTrackerStoryID("the-story-being-finished");
        storyAcceptanceInfo.setStorySHA("the-story-sha");
        when(environments.loadStoryAcceptanceDeployedAppInfo())
                .thenReturn(storyAcceptanceInfo);

        subject.start();

        verify(storyAcceptanceQueue).push("the-story-sha", "the-story-being-finished");
    }

    @Test
    public void start_deployLastProductionDeployToStoryAcceptance() throws Exception {
        Deploy lastProductionDeploy = Deploy.builder()
                .sha("some-sha").build();
        when(productionDeployHistory.getLastDeploy())
                .thenReturn(lastProductionDeploy);

        subject.start();

        verify(jenkinsAPI).revertAcceptance(lastProductionDeploy);
    }

    @Test
    public void start_clearsProductionRevertFlag() throws Exception {
        subject.start();

        verify(productionRevertFlag).clear();
    }

    @Test
    public void start_removesCurrentProductionDeployFromProductionDeployList() throws Exception {
        subject.start();

        verify(productionDeployHistory).pop();
    }

    @Test
    public void start_whenRevertProductionFails_itStoresTheCause_clearsTheRevertFlag_andExits() throws Exception {
        doThrow(new RevertFailedException("the problem")).when(jenkinsAPI).revertProduction(previousDeploy);

        subject.start();

        verify(revertErrorRepository).save("the problem");
        verify(productionRevertFlag).clear();
        verify(productionDeployHistory, times(0)).pop();
        verify(pivotalTrackerAPI, times(0)).finishStory(any());
        verifyZeroInteractions(storyAcceptanceQueue);
    }

    @Test
    public void start_whenStoryAcceptanceRevertFails_itStoresTheCause() throws Exception {
        Deploy previousDeploy = Deploy.builder()
                .sha("some-sha").build();
        when(productionDeployHistory.getPreviousDeploy())
                .thenReturn(previousDeploy);

        Deploy lastDeploy = Deploy.builder()
                .storyID("someStory")
                .sha("blahblahSha").build();
        when(productionDeployHistory.getLastDeploy())
                .thenReturn(lastDeploy);

        doThrow(new RevertFailedException("the problem")).when(jenkinsAPI).revertAcceptance(lastDeploy);

        subject.start();

        verify(revertErrorRepository).save("the problem");
        verify(productionRevertFlag).clear();
    }

    @Test
    public void whenProductionAndStoryAcceptanceDeploysMatch_skipSADeployAndSAQueueManipulation() throws Exception {
        DeployedAppInfo storyAcceptanceInfo = new DeployedAppInfo();
        storyAcceptanceInfo.setPivotalTrackerStoryID("same-as-prod");
        storyAcceptanceInfo.setStorySHA("same-sha-as-prod");
        when(environments.loadStoryAcceptanceDeployedAppInfo())
                .thenReturn(storyAcceptanceInfo);

        when(productionDeployHistory.getLastDeploy())
                .thenReturn(Deploy.builder()
                        .storyID("same-as-prod")
                        .sha("same-sha-as-prod").build());

        subject.start();
        verify(pivotalTrackerAPI, times(0)).finishStory(any());
        verify(jenkinsAPI, times(0)).revertAcceptance(any());
        verifyZeroInteractions(storyAcceptanceQueue);
    }
}