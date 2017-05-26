package com.projectmonitor.jenkins;

import com.projectmonitor.deploys.Deploy;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class JenkinsRevertJobTest {

    private JenkinsRevertJob subject;
    @Mock
    private JenkinsJobAPI jenkinsJobAPI;
    @Mock
    private JenkinsJobPoller jenkinsJobPoller;
    private CIJobConfiguration ciJobConfiguration;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        ciJobConfiguration = new CIJobConfiguration();
        ciJobConfiguration.setRevertProductionURL("http://revert.job/");
        ciJobConfiguration.setRevertProductionStatusURL("jobstatus");
        subject = new JenkinsRevertJob(ciJobConfiguration, jenkinsJobAPI, jenkinsJobPoller);
    }

    @Test
    public void execute_triggersTheRevertToSAJob() throws Exception {
        Deploy theDeploy = Deploy.builder()
                .sha("sha")
                .storyID("storyID")
                .build();
        subject.execute(theDeploy);

        verify(jenkinsJobAPI).triggerJob("http://revert.job/" + "sha&STORY_ID=storyID");
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
    public void whenTheRevertJobRequestFails_throwsAnException() throws Exception {
        doThrow(new RequestFailedException("an issue")).when(jenkinsJobAPI).triggerJob(any());

        Deploy theDeploy = Deploy.builder()
                .sha("sha")
                .storyID("storyID")
                .build();

        exception.expect(RevertFailedException.class);
        exception.expectMessage("an issue");
        subject.execute(theDeploy);
    }
}