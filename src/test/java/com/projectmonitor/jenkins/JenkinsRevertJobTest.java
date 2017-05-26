package com.projectmonitor.jenkins;

import com.projectmonitor.deploypipeline.Deploy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class JenkinsRevertJobTest {

    private JenkinsRevertJob subject;
    @Mock
    private JenkinsRestTemplate jenkinsRestTemplate;
    @Mock
    private JenkinsJobPoller jenkinsJobPoller;
    private CIJobConfiguration ciJobConfiguration;

    @Before
    public void setUp() {
        ciJobConfiguration = new CIJobConfiguration();
        ciJobConfiguration.setRevertProductionURL("http://revert.job/");
        ciJobConfiguration.setRevertProductionStatusURL("jobstatus");
        subject = new JenkinsRevertJob(ciJobConfiguration, jenkinsRestTemplate, jenkinsJobPoller);
    }

    @Test
    public void execute_triggersTheRevertToSAJob() throws Exception {
        Deploy theDeploy = Deploy.builder()
                .sha("sha")
                .storyID("storyID")
                .build();
        subject.execute(theDeploy);

        verify(jenkinsRestTemplate).triggerJob("http://revert.job/" + "sha&STORY_ID=storyID");
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
    public void whenTheRevertJobIsUnResponsive_returnFalse() throws Exception {
        fail();
    }
}