package com.projectmonitor.jenkins;

import com.projectmonitor.deploys.Deploy;

public interface JenkinsAPI {

    CIResponse loadLastCompletedCIRun();

    CIResponse loadStoryAcceptanceLastDeployStatus();

    CIResponse loadProductionLastDeployStatus();

    void revertProduction(Deploy deploy) throws RevertFailedException;

    void revertAcceptance(Deploy deploy) throws RevertFailedException;
}
