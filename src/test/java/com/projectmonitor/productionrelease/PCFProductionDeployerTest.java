package com.projectmonitor.productionrelease;

import com.projectmonitor.CIJobConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PCFProductionDeployerTest {

    PCFProductionDeployer subject;

    String expectedProductionDeployJobURL = "http://localhost:8080/job/TestProject to Production/buildWithParameters?SHA_TO_DEPLOY=blahblahSHA&STORY_ID=theStoryID";
    String deployStatusURL = "http://localhost:8080/job/TestProject to Production/lastBuild/api/json";

    @Mock
    RestTemplate productionReleaseRestTemplate;

    @Mock
    ThreadSleepService threadSleepService;

    CIJobConfiguration ciJobConfiguration;

    @Before
    public void setUp(){
        ciJobConfiguration = new CIJobConfiguration();
        ciJobConfiguration.setProductionDeployJobURL("http://localhost:8080/job/TestProject to Production/buildWithParameters?SHA_TO_DEPLOY=");
        ciJobConfiguration.setProductionDeployStatusURL(deployStatusURL);
        subject = new PCFProductionDeployer(productionReleaseRestTemplate, threadSleepService, ciJobConfiguration);
    }

    @Test
    public void push_kicksOffProductionDeployJob_withShaFromAcceptance() throws Exception {
        when(productionReleaseRestTemplate.getForObject(deployStatusURL, JenkinsJobStatus.class)).thenReturn(new JenkinsJobStatus());
        subject.push("blahblahSHA", "theStoryID");
        Mockito.verify(productionReleaseRestTemplate).getForObject(expectedProductionDeployJobURL, Object.class);
    }

    @Test
    public void push_whenProdDeployFails_returnsFalse() throws Exception {
        JenkinsJobStatus prodDeployStatus = new JenkinsJobStatus();
        prodDeployStatus.setBuilding(false);
        prodDeployStatus.setResult("NOT_A_SUCCESS");

        when(productionReleaseRestTemplate.getForObject(deployStatusURL, JenkinsJobStatus.class)).thenReturn(prodDeployStatus);
        assertThat(subject.push("blahblahSHA", "theStoryID")).isFalse();
    }

    @Test
    public void push_whenProdDeployStillRunning_pollsJobEveryFewSecondsUntilCompletion_returnsTrueWhenBuildSucceeds() throws Exception {
        JenkinsJobStatus prodDeployStatus = new JenkinsJobStatus();
        prodDeployStatus.setBuilding(true);

        JenkinsJobStatus successProdDeployStatus = new JenkinsJobStatus();
        successProdDeployStatus.setBuilding(false);
        successProdDeployStatus.setResult(PCFProductionDeployer.jenkinsSuccessMessage);

        when(productionReleaseRestTemplate.getForObject(deployStatusURL, JenkinsJobStatus.class))
                .thenReturn(prodDeployStatus).thenReturn(successProdDeployStatus);
        assertThat(subject.push("blahblahSHA", "theStoryID")).isTrue();
    }
}