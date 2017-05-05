package com.projectmonitor.pivotaltracker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoveRejectLabelServiceTest {

    private RemoveRejectLabelService subject;
    @Mock
    private URLGenerator urlGenerator;
    @Mock
    private PivotalTrackerStoryConfiguration pivotalTrackerStoryConfiguration;
    @Mock
    private RestTemplate productionReleaseRestTemplate;
    @Mock
    private GetStoryService getStoryService;
    private PivotalTrackerStory theStory;

    @Before
    public void setUp() {
        PivotalTrackerLabel label = new PivotalTrackerLabel();
        label.setName("rejected");
        label.setId("someID");

        theStory = PivotalTrackerStory.builder()
                .rejectedLabelID("someID")
                .build();

        when(urlGenerator.generate("55"))
                .thenReturn("http://tracker.com/100/55");
        when(pivotalTrackerStoryConfiguration.getPivotalTrackerToken()).thenReturn("some-tracker-token");
        when(getStoryService.load("55")).thenReturn(theStory);
        subject = new RemoveRejectLabelService(urlGenerator, pivotalTrackerStoryConfiguration,
                productionReleaseRestTemplate, getStoryService);
    }

    @Test
    public void postsLabelRemovalRequestToTrackerApi() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-TrackerToken", "some-tracker-token");
        headers.set("Content-Type", "application/json");

        HttpEntity<PivotalTrackerStoryDTO> entity = new HttpEntity<>(null, headers);

        Mockito.when(productionReleaseRestTemplate.exchange(
                eq("http://tracker.com/100/55/labels/someID"),
                eq(HttpMethod.DELETE),
                eq(entity),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.ACCEPTED));

        subject.execute("55");

        Mockito.verify(productionReleaseRestTemplate).exchange(
                eq("http://tracker.com/100/55/labels/someID"),
                eq(HttpMethod.DELETE),
                eq(entity),
                eq(String.class));
    }
}