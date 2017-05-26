package com.projectmonitor.jenkins;

import com.projectmonitor.deploys.Deploy;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JenkinsRevertStoryAcceptanceTest {

    @InjectMocks
    private JenkinsRevertStoryAcceptance subject;

    @Mock
    private JenkinsJobAPI jenkinsJobAPI;

    @Mock
    private CIJobConfiguration ciJobConfiguration;

    @Mock
    private JenkinsJobPoller jenkinsJobPoller;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        when(ciJobConfiguration.getRevertStoryAcceptanceURL())
                .thenReturn("theRevertJob.com/");

        when(ciJobConfiguration.getRevertStoryAcceptanceStatusURL())
                .thenReturn("jobstatus");
    }

    @Test
    public void execute_triggersJenkinsSARevertJob() throws Exception {
        Deploy theDeploy = Deploy.builder()
                .sha("sha").storyID("storyID").build();
        subject.execute(theDeploy);
        verify(jenkinsJobAPI).triggerJob("theRevertJob.com/sha&STORY_ID=storyID");
    }

    @Test
    public void pollsForDeployCompletion() throws Exception {
        Deploy theDeploy = Deploy.builder()
                .sha("sha")
                .storyID("storyID")
                .build();
        subject.execute(theDeploy);

        verify(jenkinsJobPoller).execute("jobstatus");
    }

    @Test
    public void whenTriggerJobRequestFails_throwsException() throws Exception {
        Deploy theDeploy = Deploy.builder()
                .sha("sha")
                .storyID("storyID")
                .build();

        RequestFailedException theError = new RequestFailedException("done goofed");
        doThrow(theError).when(jenkinsJobAPI).triggerJob(any());

        exception.expect(RevertFailedException.class);
        exception.expectMessage("done goofed");
        subject.execute(theDeploy);
    }
}