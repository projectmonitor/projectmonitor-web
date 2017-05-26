package com.projectmonitor.jenkins;

import com.projectmonitor.deploys.Deploy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class JenkinsJobsTest {

    @InjectMocks
    private JenkinsJobs subject;

    @Mock
    private JenkinsClient jenkinsClient;
    @Mock
    private CIJobConfiguration ciJobConfiguration;
    @Mock
    private JenkinsRevertJob jenkinsRevertJob;
    @Mock
    private JenkinsRevertStoryAcceptance jenkinsRevertStoryAcceptance;

    @Test
    public void revertProduction_delegates() throws Exception {
        Deploy theDeploy = Deploy.builder().build();
        subject.revertProduction(theDeploy);
        verify(jenkinsRevertJob).execute(theDeploy);
    }

    @Test
    public void deployToStoryAcceptance_delegates() throws Exception {
        Deploy theDeploy = Deploy.builder().build();
        subject.revertAcceptance(theDeploy);
        verify(jenkinsRevertStoryAcceptance).execute(theDeploy);
    }
}