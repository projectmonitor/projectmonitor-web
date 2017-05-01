package com.projectmonitor.productionrelease;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PivotalTrackerAPITest {

    private PivotalTrackerAPI subject;
    @Mock
    private PivotalTrackerStoryConfiguration pivotalTrackerStoryConfiguration;
    @Mock
    private RestTemplate productionReleaseRestTemplate;

    @Before
    public void setUp() {
        subject = new PivotalTrackerAPI(pivotalTrackerStoryConfiguration, productionReleaseRestTemplate);
        when(pivotalTrackerStoryConfiguration.getPivotalTrackerStoryDetailsUrl())
                .thenReturn("http://tracker.com/{TRACKER_PROJECT_ID}/{STORY_ID}");
        when(pivotalTrackerStoryConfiguration.getTrackerProjectId()).thenReturn("100");
    }

    @Test
    public void rejectStory_informsTrackerTheStoryHasBeenRejected() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-TrackerToken", "some-tracker-token");
        headers.set("Content-Type", "application/json");

        PivotalTrackerStory body = new PivotalTrackerStory();
        body.setCurrentState("rejected");

        Mockito.when(productionReleaseRestTemplate.exchange(
                eq("http://tracker.com/100/55"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.FORBIDDEN));

        subject.rejectStory("55");
        Mockito.verify(productionReleaseRestTemplate).exchange(
                eq("http://tracker.com/100/55"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(String.class));
    }

    @Test
    public void getStory_test() throws Exception {
        when(pivotalTrackerStoryConfiguration.getPivotalTrackerStoryDetailsUrl()).thenReturn("https://trackerapi.com/{TRACKER_PROJECT_ID}/{STORY_ID}");
        when(pivotalTrackerStoryConfiguration.getTrackerProjectId()).thenReturn("989898");
        PivotalTrackerStory story = new PivotalTrackerStory();

        when(productionReleaseRestTemplate.getForObject("https://trackerapi.com/989898/8888", PivotalTrackerStory.class))
                .thenReturn(story);
        assertThat(subject.getStory("8888")).isEqualTo(story);
    }
}