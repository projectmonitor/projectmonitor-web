package com.projectmonitor.deploypipeline;

import com.projectmonitor.ApplicationConfiguration;
import com.projectmonitor.pivotaltracker.PivotalTrackerAPI;
import com.projectmonitor.pivotaltracker.PivotalTrackerStory;
import com.projectmonitor.projectstatus.DeployedAppInfo;
import com.projectmonitor.revertbuild.ProductionRevertFlag;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CheckCurrentAcceptanceStoryStatusServiceTest {

    private CheckCurrentAcceptanceStoryStatusService subject;
    private ApplicationConfiguration applicationConfiguration;
    @Mock
    private RestTemplate productionReleaseRestTemplate;
    @Mock
    private PCFProductionDeployer pcfProductionDeployer;
    @Mock
    private PCFStoryAcceptanceDeployer pcfStoryAcceptanceDeployer;
    @Mock
    private PivotalTrackerAPI pivotalTrackerAPI;
    @Mock
    private ProductionRevertFlag productionRevertFlag;

    private DeployedAppInfo acceptanceStoryInfo;
    private DeployedAppInfo productionStoryInfo;

    @Before
    public void setUp() {
        applicationConfiguration = new ApplicationConfiguration();
        applicationConfiguration.setStoryAcceptanceUrl("http://story.acceptance.url/info");
        applicationConfiguration.setProductionUrl("http://story.production.url/info");

        subject = new CheckCurrentAcceptanceStoryStatusService(productionReleaseRestTemplate,
                applicationConfiguration, pcfProductionDeployer,
                pcfStoryAcceptanceDeployer, pivotalTrackerAPI, productionRevertFlag);

        acceptanceStoryInfo = new DeployedAppInfo();
        acceptanceStoryInfo.setPivotalTrackerStoryID("8888");
        acceptanceStoryInfo.setStorySHA("blahblahSHA");
        when(productionReleaseRestTemplate.getForObject(applicationConfiguration.getStoryAcceptanceUrl(), DeployedAppInfo.class)).thenReturn(acceptanceStoryInfo);

        productionStoryInfo = new DeployedAppInfo();
        productionStoryInfo.setPivotalTrackerStoryID("9999");
        when(productionReleaseRestTemplate.getForObject(applicationConfiguration.getProductionUrl(), DeployedAppInfo.class)).thenReturn(productionStoryInfo);
        when(productionRevertFlag.get()).thenReturn(false);
    }

    @Test
    public void execute_whenTheProductionRevertFlagIsSet_exitOnStartup() throws Exception {
        when(productionRevertFlag.get()).thenReturn(true);
        subject.execute();
        verifyZeroInteractions(productionReleaseRestTemplate);
    }

    @Test
    public void execute_whenTrackerAPIResponds_andStoryisAccepted_andStoriesDiffer_triggersAProductionDeploy() {
        PivotalTrackerStory acceptedStory = PivotalTrackerStory.builder()
                .currentState("accepted").build();

        when(pivotalTrackerAPI.getStory("8888")).thenReturn(acceptedStory);

        subject.execute();
        verify(pcfProductionDeployer).push(acceptanceStoryInfo.getStorySHA(), acceptanceStoryInfo.getPivotalTrackerStoryID());
    }

    @Test
    public void execute_whenTrackerAPIResponds_andStoryisAccepted_andStoriesMatch_doesNotTriggerAProductionDeploy_butDoesTriggerAnSADeploy() {
        PivotalTrackerStory acceptedStory = PivotalTrackerStory.builder()
                .currentState("accepted").build();

        when(pivotalTrackerAPI.getStory("8888")).thenReturn(acceptedStory);

        productionStoryInfo.setPivotalTrackerStoryID("8888");

        subject.execute();

        verify(pcfProductionDeployer, never()).push(acceptanceStoryInfo.getStorySHA(), acceptanceStoryInfo.getPivotalTrackerStoryID());
        verify(pcfStoryAcceptanceDeployer).push();
    }

    @Test
    public void execute_whenTrackerAPIResponds_andStoryIsNotAccepted_doesNotTriggerADeployToAnyEnvironment() throws Exception {
        PivotalTrackerStory notAcceptedStory = PivotalTrackerStory.builder()
                .currentState("delivered").build();

        when(pivotalTrackerAPI.getStory("8888")).thenReturn(notAcceptedStory);
        subject.execute();
        verify(pcfProductionDeployer, never()).push(acceptanceStoryInfo.getStorySHA(), acceptanceStoryInfo.getPivotalTrackerStoryID());
        verify(pcfStoryAcceptanceDeployer, never()).push();
    }

    @Test
    public void execute_whenTrackerAPIResponds_andStoryIsRejected_triggersContingentStoryAcceptaceDeploy_AND_removesRejectedLabel() throws Exception {
        PivotalTrackerStory rejectedStory = PivotalTrackerStory.builder()
                .currentState("rejected")
                .hasBeenRejected(false)
                .build();

        when(pivotalTrackerAPI.getStory("8888")).thenReturn(rejectedStory);
        subject.execute();

        verify(pcfProductionDeployer, never()).push(acceptanceStoryInfo.getStorySHA(), acceptanceStoryInfo.getPivotalTrackerStoryID());
        verify(pcfStoryAcceptanceDeployer).pushRejectedBuild(acceptanceStoryInfo.getPivotalTrackerStoryID());
    }

    @Test
    public void execute_whenTrackerAPIResponds_andStoryHasBeenRejected_triggersContingentStoryAcceptaceDeploy_AND_removesRejectedLabel() throws Exception {
        PivotalTrackerStory rejectedStory = PivotalTrackerStory.builder()
                .currentState("does not matter")
                .hasBeenRejected(true)
                .build();

        when(pivotalTrackerAPI.getStory("8888")).thenReturn(rejectedStory);
        subject.execute();
        verify(pcfProductionDeployer, never()).push(acceptanceStoryInfo.getStorySHA(), acceptanceStoryInfo.getPivotalTrackerStoryID());
        verify(pcfStoryAcceptanceDeployer).pushRejectedBuild(acceptanceStoryInfo.getPivotalTrackerStoryID());
    }

    @Test
    public void execute_whenAProductionDeployFails_doesNotTriggerAStoryAcceptanceDeploy() throws Exception {
        PivotalTrackerStory acceptedStory = PivotalTrackerStory.builder()
                .currentState("accepted").build();

        when(pivotalTrackerAPI.getStory("8888")).thenReturn(acceptedStory);
        when(pcfProductionDeployer.push(acceptanceStoryInfo.getStorySHA(), acceptanceStoryInfo.getPivotalTrackerStoryID())).thenReturn(false);

        subject.execute();
        verify(pcfProductionDeployer).push(acceptanceStoryInfo.getStorySHA(), acceptanceStoryInfo.getPivotalTrackerStoryID());
        verify(pcfStoryAcceptanceDeployer, never()).push();
    }

    @Test
    public void execute_whenAProductionDeploySucceeds_triggersADeployToStoryAcceptance() throws Exception {
        PivotalTrackerStory acceptedStory = PivotalTrackerStory.builder()
                .currentState("accepted").build();

        when(pivotalTrackerAPI.getStory("8888")).thenReturn(acceptedStory);
        when(pcfProductionDeployer.push(acceptanceStoryInfo.getStorySHA(), acceptanceStoryInfo.getPivotalTrackerStoryID())).thenReturn(true);

        subject.execute();
        verify(pcfProductionDeployer).push(acceptanceStoryInfo.getStorySHA(), acceptanceStoryInfo.getPivotalTrackerStoryID());
        verify(pcfStoryAcceptanceDeployer).push();
    }
}