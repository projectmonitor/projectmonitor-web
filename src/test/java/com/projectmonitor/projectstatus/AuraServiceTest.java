package com.projectmonitor.projectstatus;

import com.projectmonitor.jenkins.CIResponse;
import com.projectmonitor.pivotaltracker.PivotalTrackerStory;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AuraServiceTest {

    AuraService subject;

    private CIResponse ciStatusResponse;
    private CIResponse storyAcceptanceDeployResponse;
    private DeployedAppInfo storyAcceptanceDeployedAppInfo;
    private CIResponse productionDeployResponse;
    private DeployedAppInfo productionDeployedAppInfo;
    private PivotalTrackerStory pivotalTrackerStory;

    private String aura;

    @Before
    public void setUp(){
        ciStatusResponse = new CIResponse();
        storyAcceptanceDeployResponse = new CIResponse();
        storyAcceptanceDeployedAppInfo = new DeployedAppInfo();
        productionDeployResponse = new CIResponse();
        productionDeployedAppInfo = new DeployedAppInfo();
        pivotalTrackerStory = PivotalTrackerStory.builder().build();
        subject = new AuraService();
    }

    @Test
    public void whenAllChecksAreOK_returnsNormal(){
        aura = subject.determineAura(ciStatusResponse, storyAcceptanceDeployResponse,
                storyAcceptanceDeployedAppInfo, productionDeployResponse,
                productionDeployedAppInfo, pivotalTrackerStory);

        assertThat(aura).isEqualTo("normal");
    }

    @Test
    public void whenCIJobResultIsNotResponding_returnsDependencyDown() throws Exception {
        ciStatusResponse.setResult("CI is not responding");

        aura = subject.determineAura(ciStatusResponse, storyAcceptanceDeployResponse,
                storyAcceptanceDeployedAppInfo, productionDeployResponse,
                productionDeployedAppInfo, pivotalTrackerStory);

        assertThat(aura).isEqualTo("dependencyDown");
    }

    @Test
    public void whenCIJobFailed_returnsCIFailed() throws Exception {
        ciStatusResponse.setResult("FAILURE");

        aura = subject.determineAura(ciStatusResponse, storyAcceptanceDeployResponse,
                storyAcceptanceDeployedAppInfo, productionDeployResponse,
                productionDeployedAppInfo, pivotalTrackerStory);

        assertThat(aura).isEqualTo("ciFailed");
    }

    @Test
    public void whenStoryAcceptanceDeployStatusJobDoesNotRespond_returnsDependencyDown() throws Exception {
        storyAcceptanceDeployResponse.setResult("Story Acceptance Deploy Job is not responding");
        aura = subject.determineAura(ciStatusResponse, storyAcceptanceDeployResponse,
                storyAcceptanceDeployedAppInfo, productionDeployResponse,
                productionDeployedAppInfo, pivotalTrackerStory);

        assertThat(aura).isEqualTo("dependencyDown");
    }

    @Test
    public void whenProductionDeployJobDoesNotRespond_returnsDependencyDown() throws Exception {
        productionDeployResponse.setResult("Production Deploy Job is not responding");

        aura = subject.determineAura(ciStatusResponse, storyAcceptanceDeployResponse,
                storyAcceptanceDeployedAppInfo, productionDeployResponse,
                productionDeployedAppInfo, pivotalTrackerStory);

        assertThat(aura).isEqualTo("dependencyDown");
    }

    @Test
    public void whenStoryAcceptanceDeployHasFailed_takesPrecedenceOverCIFailures_returnsSADeployFailed() throws Exception {
        ciStatusResponse.setResult("CI is not responding");
        productionDeployResponse.setResult("Production Deploy Job is not responding");
        storyAcceptanceDeployResponse.setResult("FAILURE");

        aura = subject.determineAura(ciStatusResponse, storyAcceptanceDeployResponse,
                storyAcceptanceDeployedAppInfo, productionDeployResponse,
                productionDeployedAppInfo, pivotalTrackerStory);

        assertThat(aura).isEqualTo("storyAcceptanceDeployFailed");
    }

    @Test
    public void whenStoryAcceptanceEnvironmentIsDown_takesPrecedenceOverNonProductionJobFailures_returnsAccpetanceDown() throws Exception {
        ciStatusResponse.setResult("FAILURE");
        productionDeployResponse.setResult("happy as a bee");
        storyAcceptanceDeployResponse.setResult("FAILURE");
        storyAcceptanceDeployedAppInfo.setPivotalTrackerStoryID("Story Acceptance is not responding");

        aura = subject.determineAura(ciStatusResponse, storyAcceptanceDeployResponse,
                storyAcceptanceDeployedAppInfo, productionDeployResponse,
                productionDeployedAppInfo, pivotalTrackerStory);

        assertThat(aura).isEqualTo("storyAcceptanceDown");
    }

    @Test
    public void whenStoryInAcceptanceHasBeenRejected_takesPrecedenceOverNonProductionFailures_returnsStoryRejected() throws Exception {
        ciStatusResponse.setResult("FAILURE");
        productionDeployResponse.setResult("happy as a bee");
        storyAcceptanceDeployResponse.setResult("FAILURE");
        storyAcceptanceDeployedAppInfo.setPivotalTrackerStoryID("Story Acceptance is not responding");
        pivotalTrackerStory = PivotalTrackerStory.builder()
                .currentState("rejected")
                .hasBeenRejected(true)
                .build();

        aura = subject.determineAura(ciStatusResponse, storyAcceptanceDeployResponse,
                storyAcceptanceDeployedAppInfo, productionDeployResponse,
                productionDeployedAppInfo, pivotalTrackerStory);

        assertThat(aura).isEqualTo("storyRejected");
    }

    @Test
    public void whenProductionDeployHasFailed_takesPrecedenceOVerCiAndSAFailures_returnsProductionDeployFailed() throws Exception {
        ciStatusResponse.setResult("CI is not responding");
        productionDeployResponse.setResult("FAILURE");
        storyAcceptanceDeployResponse.setResult("FAILURE");
        pivotalTrackerStory = PivotalTrackerStory.builder().currentState("rejected").build();
        storyAcceptanceDeployedAppInfo.setPivotalTrackerStoryID("Story Acceptance is not responding");

        aura = subject.determineAura(ciStatusResponse, storyAcceptanceDeployResponse,
                storyAcceptanceDeployedAppInfo, productionDeployResponse,
                productionDeployedAppInfo, pivotalTrackerStory);

        assertThat(aura).isEqualTo("productionDeployFailed");
    }

    @Test
    public void whenProductionIsDown_takesPrecedenceOverAll_returnsProductionDown() throws Exception {
        ciStatusResponse.setResult("FAILURE");
        productionDeployResponse.setResult("FAILURE");
        storyAcceptanceDeployResponse.setResult("FAILURE");
        pivotalTrackerStory = PivotalTrackerStory.builder().currentState("rejected").build();
        storyAcceptanceDeployedAppInfo.setPivotalTrackerStoryID("Story Acceptance is not responding");
        productionDeployedAppInfo.setPivotalTrackerStoryID("Production is not responding");

        aura = subject.determineAura(ciStatusResponse, storyAcceptanceDeployResponse,
                storyAcceptanceDeployedAppInfo, productionDeployResponse,
                productionDeployedAppInfo, pivotalTrackerStory);

        assertThat(aura).isEqualTo("productionDown");
    }
}