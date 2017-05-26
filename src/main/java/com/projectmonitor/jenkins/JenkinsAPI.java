package com.projectmonitor.jenkins;

import com.projectmonitor.deploypipeline.Deploy;

public interface JenkinsAPI {

    CIResponse loadLastCompletedCIRun();

    CIResponse loadStoryAcceptanceLastDeployStatus();

    CIResponse loadProductionLastDeployStatus();

    void revertProduction(Deploy deploy) throws RevertFailedException;

    void deployToStoryAcceptance(Deploy deploy) throws RevertFailedException;
}
