package com.projectmonitor.deployjob;

import com.projectmonitor.deploys.ProductionDeployHistory;
import com.projectmonitor.jenkins.CIJobConfiguration;
import com.projectmonitor.jenkins.CIResponse;
import com.projectmonitor.jenkins.JenkinsJobPoller;
import com.projectmonitor.jenkins.JenkinsJobAPI;
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
    private JenkinsJobAPI jenkinsJobAPI;
    @Mock
    private ProductionDeployHistory productionDeployHistory;
    @Mock
    JenkinsJobPoller jenkinsJobPoller;

    @Before
    public void setUp() {
        CIJobConfiguration ciJobConfiguration = new CIJobConfiguration();
        ciJobConfiguration.setProductionDeployJobURL("http://localhost:8080/job/TestProject to Production/buildWithParameters?SHA_TO_DEPLOY=");
        ciJobConfiguration.setProductionDeployStatusURL(deployStatusURL);
        subject = new PCFProductionDeployer(jenkinsJobAPI, ciJobConfiguration, productionDeployHistory, jenkinsJobPoller);
    }

    @Test
    public void push_kicksOffProductionDeployJob_withShaFromAcceptance() throws Exception {
        when(jenkinsJobAPI.loadJobStatus(deployStatusURL)).thenReturn(new CIResponse());
        subject.push("blahblahSHA", "theStoryID");
        Mockito.verify(jenkinsJobAPI).triggerJob(expectedProductionDeployJobURL);
    }

    @Test
    public void push_whenProdDeployFails_returnsFalseAndDoesNotAddToProductionDeployQueue() throws Exception {
        when(jenkinsJobPoller.execute(deployStatusURL)).thenReturn(false);
        assertThat(subject.push("blahblahSHA", "theStoryID")).isFalse();
        verifyZeroInteractions(productionDeployHistory);
    }

    @Test
    public void push_whenProdDeployHasSucceeded_addsAnEntryToProductionDeploys() throws Exception {
        when(jenkinsJobPoller.execute(deployStatusURL)).thenReturn(true);
        assertThat(subject.push("blahblahSHA", "theStoryID")).isTrue();
        verify(productionDeployHistory).push("blahblahSHA", "theStoryID");
    }
}