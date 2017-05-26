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
public class StoryStateServiceTest {

    private StoryStateService subject;
    @Mock
    private PivotalTrackerStoryConfiguration pivotalTrackerStoryConfiguration;
    @Mock
    private URLGenerator urlGenerator;
    @Mock
    private RestTemplate productionReleaseRestTemplate;

    @Before
    public void setUp() {
        when(urlGenerator.generate("55"))
                .thenReturn("http://tracker.com/100/55");

        when(pivotalTrackerStoryConfiguration.getPivotalTrackerToken()).thenReturn("some-tracker-token");

        subject = new StoryStateService(urlGenerator, pivotalTrackerStoryConfiguration,
                productionReleaseRestTemplate);
    }

    @Test
    public void execute_informsTrackerOfTheStateChange() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-TrackerToken", "some-tracker-token");
        headers.set("Content-Type", "application/json");

        PivotalTrackerStoryDTO pivotalTrackerStoryDTO = new PivotalTrackerStoryDTO();
        pivotalTrackerStoryDTO.setCurrentState("rejected");
        HttpEntity<PivotalTrackerStoryDTO> entity = new HttpEntity<>(pivotalTrackerStoryDTO, headers);

        Mockito.when(productionReleaseRestTemplate.exchange(
                eq("http://tracker.com/100/55"),
                eq(HttpMethod.PUT),
                eq(entity),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.ACCEPTED));

        subject.setState("55", "rejected");
        Mockito.verify(productionReleaseRestTemplate).exchange(
                eq("http://tracker.com/100/55"),
                eq(HttpMethod.PUT),
                eq(entity),
                eq(String.class));
    }
}