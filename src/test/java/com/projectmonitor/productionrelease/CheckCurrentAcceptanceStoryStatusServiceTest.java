package com.projectmonitor.productionrelease;

import com.projectmonitor.ApplicationConfiguration;
import com.projectmonitor.ProjectMonitorTrackerStoryInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CheckCurrentAcceptanceStoryStatusServiceTest {

    private CheckCurrentAcceptanceStoryStatusService subject;
    @Mock
    private RestTemplate productionReleaseRestTemplate;
    private ApplicationConfiguration applicationConfiguration;
    @Mock
    private PivotalTrackerStoryConfiguration pivotalTrackerStoryConfiguration;
    @Mock
    PCFDeployer pcfDeployer;
    private com.projectmonitor.ProjectMonitorTrackerStoryInfo acceptanceStoryInfo;
    private com.projectmonitor.ProjectMonitorTrackerStoryInfo productionStoryInfo;

    @Before
    public void setUp() {
        applicationConfiguration = new ApplicationConfiguration();
        applicationConfiguration.setStoryAcceptanceUrl("http://story.acceptance.url/");
        applicationConfiguration.setProductionUrl("http://story.production.url/");

        subject = new CheckCurrentAcceptanceStoryStatusService(productionReleaseRestTemplate, pivotalTrackerStoryConfiguration, applicationConfiguration, pcfDeployer);
        when(pivotalTrackerStoryConfiguration.getPivotalTrackerStoryDetailsUrl()).thenReturn("https://trackerapi.com/{STORY_ID}");

        acceptanceStoryInfo = new ProjectMonitorTrackerStoryInfo();
        acceptanceStoryInfo.setPivotalTrackerStoryID("8888");
        when(productionReleaseRestTemplate.getForObject(applicationConfiguration.getStoryAcceptanceUrl() + "info", ProjectMonitorTrackerStoryInfo.class)).thenReturn(acceptanceStoryInfo);

        productionStoryInfo = new ProjectMonitorTrackerStoryInfo();
        productionStoryInfo.setPivotalTrackerStoryID("9999");
        when(productionReleaseRestTemplate.getForObject(applicationConfiguration.getProductionUrl() + "info", ProjectMonitorTrackerStoryInfo.class)).thenReturn(productionStoryInfo);
    }

    @Test
    public void execute_whenTrackerAPIResponds_andStoryisAccepted_andStoriesDiffer_triggersAProductionDeploy() {
        PivotalTrackerStory acceptedStory = new PivotalTrackerStory();
        acceptedStory.setCurrentState("accepted");

        when(productionReleaseRestTemplate.getForObject("https://trackerapi.com/8888", PivotalTrackerStory.class))
                .thenReturn(acceptedStory);

        subject.execute();
        verify(pcfDeployer).push();
    }

    @Test
    public void execute_whenTrackerAPIResponds_andStoryisAccepted_andStoriesMatch_doesNottriggerAProductionDeploy() {
        PivotalTrackerStory acceptedStory = new PivotalTrackerStory();
        acceptedStory.setCurrentState("accepted");

        when(productionReleaseRestTemplate.getForObject("https://trackerapi.com/8888", PivotalTrackerStory.class))
                .thenReturn(acceptedStory);

        productionStoryInfo.setPivotalTrackerStoryID("8888");

        subject.execute();

        verify(pcfDeployer, never()).push();
    }

    @Test
    public void execute_whenTrackerAPIResponds_andStoryIsNotAccepted_doesNotTriggerAProductionDeploy() throws Exception {
        PivotalTrackerStory notAcceptedStory = new PivotalTrackerStory();
        notAcceptedStory.setCurrentState("delivered");
        when(productionReleaseRestTemplate.getForObject("https://trackerapi.com/8888", PivotalTrackerStory.class))
                .thenReturn(notAcceptedStory);
        subject.execute();
        verify(pcfDeployer, never()).push();
    }
}