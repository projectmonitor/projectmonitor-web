package com.projectmonitor.jenkins;

import com.projectmonitor.deploypipeline.Deploy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class JenkinsJobsTest {

    @InjectMocks
    private JenkinsJobs subject;

    @Mock
    private JenkinsRestTemplate jenkinsRestTemplate;
    @Mock
    private CIJobConfiguration ciJobConfiguration;
    @Mock
    private JenkinsRevertJob jenkinsRevertJob;
    @Mock
    private JenkinsRevertStoryAcceptance jenkinsRevertStoryAcceptance;

    @Test
    public void coverOtherMethods() throws Exception {
        fail();
    }

    @Test
    public void revertProduction_delegates() throws Exception {
        Deploy theDeploy = Deploy.builder().build();
        subject.revertProduction(theDeploy);
        verify(jenkinsRevertJob).execute(theDeploy);
    }

    @Test
    public void deployToStoryAcceptance_delegates() throws Exception {
        Deploy theDeploy = Deploy.builder().build();
        subject.deployToStoryAcceptance(theDeploy);
        verify(jenkinsRevertStoryAcceptance).execute(theDeploy);
    }
}