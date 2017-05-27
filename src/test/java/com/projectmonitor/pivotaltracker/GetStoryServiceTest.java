package com.projectmonitor.pivotaltracker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GetStoryServiceTest {
    @InjectMocks
    private GetStoryService subject;

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private URLGenerator urlGenerator;
    @Mock
    private RejectLabelService rejectLabelService;

    @Before
    public void setUp(){
        when(urlGenerator.generate("8888"))
                .thenReturn("https://trackerapi.com/989898/8888");
    }

    @Test
    public void load_whenTheStoryHasNoRejectedLabel_setsHasBeenRejectedToFalse() throws Exception {
        PivotalTrackerStoryDTO storyDTO = new PivotalTrackerStoryDTO();
        storyDTO.setCurrentState("whatever");
        PivotalTrackerStory story = PivotalTrackerStory.builder()
                .currentState("whatever")
                .hasBeenRejected(false)
                .rejectedLabelID("")
                .build();

        when(restTemplate.getForObject("https://trackerapi.com/989898/8888",
                PivotalTrackerStoryDTO.class))
                .thenReturn(storyDTO);
        assertThat(subject.load("8888")).isEqualTo(story);
    }

    @Test
    public void load_whenStoryIsRejectedState_ButNoLabel_addsALabel() throws Exception {
        PivotalTrackerStoryDTO storyDTO = new PivotalTrackerStoryDTO();
        storyDTO.setCurrentState("rejected");

        PivotalTrackerStory story = PivotalTrackerStory.builder()
                .currentState("rejected")
                .hasBeenRejected(true)
                .rejectedLabelID("")
                .build();

        when(restTemplate.getForObject("https://trackerapi.com/989898/8888",
                PivotalTrackerStoryDTO.class))
                .thenReturn(storyDTO);
        assertThat(subject.load("8888")).isEqualTo(story);
        Mockito.verify(rejectLabelService).add("8888");
    }

    @Test
    public void load_whenTheStoryHasARejectLabel_setsHasBeenRejectedToTrue() throws Exception {
        PivotalTrackerStoryDTO storyDTO = new PivotalTrackerStoryDTO();
        storyDTO.setCurrentState("whatever");

        PivotalTrackerLabel label = new PivotalTrackerLabel();
        label.setName("rejected");

        List<PivotalTrackerLabel> labels = new ArrayList<>();
        labels.add(label);

        storyDTO.setLabels(labels);

        PivotalTrackerStory story = PivotalTrackerStory.builder()
                .currentState("whatever")
                .hasBeenRejected(true)
                .build();

        when(restTemplate.getForObject("https://trackerapi.com/989898/8888",
                PivotalTrackerStoryDTO.class))
                .thenReturn(storyDTO);
        assertThat(subject.load("8888")).isEqualTo(story);
    }
}