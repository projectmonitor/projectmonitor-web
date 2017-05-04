package com.projectmonitor.pivotaltracker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class URLGeneratorTest {

    private URLGenerator subject;

    @Mock
    PivotalTrackerStoryConfiguration pivotalTrackerStoryConfiguration;

    @Before
    public void setup(){

        when(pivotalTrackerStoryConfiguration.getPivotalTrackerStoryDetailsUrl())
                .thenReturn("http://tracker.com/{TRACKER_PROJECT_ID}/{STORY_ID}");
        when(pivotalTrackerStoryConfiguration.getTrackerProjectId()).thenReturn("100");
        when(pivotalTrackerStoryConfiguration.getPivotalTrackerToken()).thenReturn("some-tracker-token");

        subject = new URLGenerator(pivotalTrackerStoryConfiguration);
    }

    @Test
    public void generate_replacesTheProjectID_AND_StoryID() throws Exception {
        assertThat(subject.generate("8888"))
                .isEqualTo("http://tracker.com/100/8888");
    }

}