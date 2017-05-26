package com.projectmonitor.jenkins;

import com.projectmonitor.deploypipeline.Deploy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JenkinsRevertStoryAcceptanceTest {

    @InjectMocks
    private JenkinsRevertStoryAcceptance subject;

    @Mock
    private JenkinsRestTemplate jenkinsRestTemplate;

    @Mock
    private CIJobConfiguration ciJobConfiguration;

    @Mock
    private JenkinsJobPoller jenkinsJobPoller;

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
        verify(jenkinsRestTemplate).triggerJob("theRevertJob.com/sha&STORY_ID=storyID");
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
}