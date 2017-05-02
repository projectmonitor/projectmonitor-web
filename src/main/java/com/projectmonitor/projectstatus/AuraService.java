package com.projectmonitor.projectstatus;

import com.projectmonitor.jenkins.CIResponse;
import org.springframework.stereotype.Component;

@Component
public class AuraService {
    public String determineAura(CIResponse ciResponse,
                                CIResponse storyAcceptanceDeployResponse,
                                DeployedAppInfo storyAcceptanceDeployedStory,
                                CIResponse productionDeployResponse,
                                DeployedAppInfo productionDeployedStory) {
        String aura = "normal";

        if ("CI is not responding".equals(ciResponse.getResult())) {
            aura = "dependencyDown";
        }

        if ("FAILURE".equals(ciResponse.getResult())) {
            aura = "ciFailed";
        }

        if ("Story Acceptance Deploy Job is not responding".equals(storyAcceptanceDeployResponse.getResult())) {
            aura = "dependencyDown";
        }

        if ("Production Deploy Job is not responding".equals(productionDeployResponse.getResult())) {
            aura = "dependencyDown";
        }

        if ("FAILURE".equals(storyAcceptanceDeployResponse.getResult())) {
            aura = "storyAcceptanceDeployFailed";
        }

        if ("Story Acceptance is not responding".equals(storyAcceptanceDeployedStory.getPivotalTrackerStoryID())) {
            aura = "storyAcceptanceDown";
        }

        if ("FAILURE".equals(productionDeployResponse.getResult())) {
            aura = "productionDeployFailed";
        }

        if ("Production is not responding".equals(productionDeployedStory.getPivotalTrackerStoryID())) {
            aura = "productionDown";
        }

        return aura;
    }
}
