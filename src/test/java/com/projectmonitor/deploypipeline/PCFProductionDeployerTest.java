package com.projectmonitor.deploypipeline;

import com.projectmonitor.jenkins.CIJobConfiguration;
import com.projectmonitor.jenkins.CIResponse;
import com.projectmonitor.jenkins.JenkinsRestTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PCFProductionDeployerTest {

    private PCFProductionDeployer subject;

    private String expectedProductionDeployJobURL = "http://localhost:8080/job/TestProject to Production/buildWithParameters?SHA_TO_DEPLOY=blahblahSHA&STORY_ID=theStoryID";
    private String deployStatusURL = "http://localhost:8080/job/TestProject to Production/lastBuild/api/json";

    @Mock
    private JenkinsRestTemplate jenkinsRestTemplate;

    @Mock
    private ThreadSleepService threadSleepService;

    @Mock
    private ProductionDeployHistory productionDeployHistory;

    @Before
    public void setUp(){
        CIJobConfiguration ciJobConfiguration = new CIJobConfiguration();
        ciJobConfiguration.setProductionDeployJobURL("http://localhost:8080/job/TestProject to Production/buildWithParameters?SHA_TO_DEPLOY=");
        ciJobConfiguration.setProductionDeployStatusURL(deployStatusURL);
        subject = new PCFProductionDeployer(jenkinsRestTemplate, threadSleepService, ciJobConfiguration, productionDeployHistory);
    }

    @Test
    public void push_kicksOffProductionDeployJob_withShaFromAcceptance() throws Exception {
        when(jenkinsRestTemplate.loadJobStatus(deployStatusURL)).thenReturn(new CIResponse());
        subject.push("blahblahSHA", "theStoryID");
        Mockito.verify(jenkinsRestTemplate).triggerJob(expectedProductionDeployJobURL);
    }

    @Test
    public void push_whenProdDeployFails_returnsFalseAndDoesNotAddToProductionDeployQueue() throws Exception {
        CIResponse prodDeployStatus = new CIResponse();
        prodDeployStatus.setBuilding(false);
        prodDeployStatus.setResult("NOT_A_SUCCESS");

        when(jenkinsRestTemplate.loadJobStatus(deployStatusURL)).thenReturn(prodDeployStatus);
        assertThat(subject.push("blahblahSHA", "theStoryID")).isFalse();
        verifyZeroInteractions(productionDeployHistory);
    }

    @Test
    public void push_whenProdDeployStillRunning_pollsJobEveryFewSecondsUntilCompletion_returnsTrueWhenBuildSucceeds() throws Exception {
        CIResponse prodDeployStatus = new CIResponse();
        prodDeployStatus.setBuilding(true);

        CIResponse successProdDeployStatus = new CIResponse();
        successProdDeployStatus.setBuilding(false);
        successProdDeployStatus.setResult(PCFProductionDeployer.jenkinsSuccessMessage);

        when(jenkinsRestTemplate.loadJobStatus(deployStatusURL))
                .thenReturn(prodDeployStatus).thenReturn(successProdDeployStatus);
        assertThat(subject.push("blahblahSHA", "theStoryID")).isTrue();
    }

    @Test
    public void push_whenProdDeployHasSucceeded_addsAnEntryToProductionDeploys() throws Exception {
        CIResponse prodDeployStatus = new CIResponse();
        prodDeployStatus.setBuilding(true);

        CIResponse successProdDeployStatus = new CIResponse();
        successProdDeployStatus.setBuilding(false);
        successProdDeployStatus.setResult(PCFProductionDeployer.jenkinsSuccessMessage);

        when(jenkinsRestTemplate.loadJobStatus(deployStatusURL))
                .thenReturn(successProdDeployStatus);
        subject.push("blahblahSHA", "theStoryID");
        verify(productionDeployHistory).push("blahblahSHA", "theStoryID");
    }
}