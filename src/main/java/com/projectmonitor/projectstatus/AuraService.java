package com.projectmonitor.projectstatus;

import com.projectmonitor.environments.DeployedAppInfo;
import com.projectmonitor.jenkins.CIResponse;
import com.projectmonitor.pivotaltracker.PivotalTrackerStory;
import org.springframework.stereotype.Component;

@Component
class AuraService {

    String determineProductionAura(CIResponse productionDeployResponse,
                                   DeployedAppInfo productionDeployedStory) {
        String aura = "normal";

        if ("Production Deploy Job is not responding".equals(productionDeployResponse.getResult())) {
            aura = "dependencyDown";
        }

        if ("FAILURE".equals(productionDeployResponse.getResult())) {
            aura = "productionDeployFailed";
        }

        if ("Production is not responding".equals(productionDeployedStory.getPivotalTrackerStoryID())) {
            aura = "productionDown";
        }

        return aura;
    }

    String determineCIAura(CIResponse ciResponse) {
        String aura = "normal";

        if ("CI is not responding".equals(ciResponse.getResult())) {
            aura = "dependencyDown";
        }

        if ("FAILURE".equals(ciResponse.getResult())) {
            aura = "ciFailed";
        }
        return aura;
    }

    String determineStoryAcceptanceAura(CIResponse storyAcceptanceDeployResponse,
                                        DeployedAppInfo storyAcceptanceDeployedStory,
                                        PivotalTrackerStory pivotalTrackerStory) {
        String aura = "normal";
        if ("Story Acceptance Deploy Job is not responding".equals(storyAcceptanceDeployResponse.getResult())) {
            aura = "dependencyDown";
        }
        if ("FAILURE".equals(storyAcceptanceDeployResponse.getResult())) {
            aura = "storyAcceptanceDeployFailed";
        }

        if ("Story Acceptance is not responding".equals(storyAcceptanceDeployedStory.getPivotalTrackerStoryID())) {
            aura = "storyAcceptanceDown";
        }

        if ("rejected".equals(pivotalTrackerStory.getCurrentState())
                || pivotalTrackerStory.isHasBeenRejected()) {
            aura = "storyRejected";
        }

        return aura;
    }
}
