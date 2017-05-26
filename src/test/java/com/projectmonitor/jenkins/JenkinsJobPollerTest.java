package com.projectmonitor.jenkins;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JenkinsJobPollerTest {

    @InjectMocks
    private JenkinsJobPoller subject;

    @Mock
    private JenkinsClient jenkinsClient;

    @Mock
    private ThreadSleepService threadSleepService;

    @Test
    public void blocksUntilTheJenkinsJobHasCompleted_returnsTrueIfJobSuccessful() throws InterruptedException, RequestFailedException {
        CIResponse stillBuilding = new CIResponse();
        stillBuilding.setBuilding(true);

        CIResponse finishedBuilding = new CIResponse();
        finishedBuilding.setBuilding(false);
        finishedBuilding.setResult(JenkinsJobPoller.SUCCESS_MESSAGE);

        when(jenkinsClient.loadJobStatus("theurl.com"))
                .thenReturn(stillBuilding).thenReturn(finishedBuilding);

        assertThat(subject.execute("theurl.com")).isTrue();
        verify(threadSleepService, times(2)).sleep(10000);
    }

    @Test
    public void blocksUntilTheJenkinsJobHasCompleted_returnsFalseIfJobFailed() throws Exception {
        CIResponse finishedBuilding = new CIResponse();
        finishedBuilding.setBuilding(false);
        finishedBuilding.setResult("SOMETHING_ELSE");

        when(jenkinsClient.loadJobStatus("theurl.com")).thenReturn(finishedBuilding);

        assertThat(subject.execute("theurl.com")).isFalse();
    }

    @Test
    public void whenTheJobStatusCallFails_weKeepPolling() throws Exception {
        CIResponse finishedBuilding = new CIResponse();
        finishedBuilding.setBuilding(false);
        finishedBuilding.setResult(JenkinsJobPoller.SUCCESS_MESSAGE);

        when(jenkinsClient.loadJobStatus("theurl.com"))
                .thenThrow(new RuntimeException()).thenReturn(finishedBuilding);

        assertThat(subject.execute("theurl.com")).isTrue();
        verify(threadSleepService, times(2)).sleep(10000);
    }
}