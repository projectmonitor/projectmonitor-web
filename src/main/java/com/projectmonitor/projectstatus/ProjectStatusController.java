package com.projectmonitor.projectstatus;

import com.projectmonitor.ApplicationConfiguration;
import com.projectmonitor.jenkins.CIResponse;
import com.projectmonitor.jenkins.JenkinsJobs;
import com.projectmonitor.pivotaltracker.PivotalTrackerAPIService;
import com.projectmonitor.pivotaltracker.PivotalTrackerStory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class ProjectStatusController {

    private ApplicationConfiguration applicationConfiguration;
    private JenkinsJobs jenkinsJobs;
    private Environments environments;
    private AuraService auraService;
    private PivotalTrackerAPIService pivotalTrackerAPIService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public ProjectStatusController(ApplicationConfiguration applicationConfiguration,
                                   JenkinsJobs jenkinsJobs,
                                   Environments environments,
                                   AuraService auraService,
                                   PivotalTrackerAPIService pivotalTrackerAPIService) {

        this.applicationConfiguration = applicationConfiguration;
        this.jenkinsJobs = jenkinsJobs;
        this.environments = environments;
        this.auraService = auraService;
        this.pivotalTrackerAPIService = pivotalTrackerAPIService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String execute(Model model) {
        CIResponse ciResponse = jenkinsJobs.loadLastCompletedCIRun();
        CIResponse storyAcceptanceDeployResponse = jenkinsJobs.loadStoryAcceptanceLastDeployStatus();
        DeployedAppInfo storyAcceptanceDeployedStory = environments.loadStoryAcceptanceDeployStory();

        PivotalTrackerStory pivotalTrackerStory;
        if ("Story Acceptance is not responding".equals(storyAcceptanceDeployedStory.getPivotalTrackerStoryID())) {
            pivotalTrackerStory = PivotalTrackerStory.builder().build();
        } else {
            pivotalTrackerStory = pivotalTrackerAPIService.getStory(storyAcceptanceDeployedStory.getPivotalTrackerStoryID());
        }

        CIResponse productionDeployResponse = jenkinsJobs.loadProductionLastDeployStatus();
        DeployedAppInfo productionDeployedStory = environments.loadProductionDeployStory();

        String aura = auraService.determineAura(ciResponse, storyAcceptanceDeployResponse,
                storyAcceptanceDeployedStory, productionDeployResponse,
                productionDeployedStory, pivotalTrackerStory);

        model.addAttribute("status", ciResponse.getResult());
        model.addAttribute("githubUsername", applicationConfiguration.getGithubUsername());
        model.addAttribute("githubProjectName", applicationConfiguration.getGithubProjectName());
        model.addAttribute("backgroundColor", aura);
        model.addAttribute("storyStatus", pivotalTrackerStory.getCurrentState());
        model.addAttribute("storyAcceptanceDeployResponse", storyAcceptanceDeployResponse.getResult());
        model.addAttribute("productionDeployResponse", productionDeployResponse.getResult());
        model.addAttribute("storyAcceptanceDeployedStoryID", storyAcceptanceDeployedStory.getPivotalTrackerStoryID());
        model.addAttribute("storyAcceptanceDeployedSHA", storyAcceptanceDeployedStory.getStorySHA());
        model.addAttribute("productionDeployedStoryID", productionDeployedStory.getPivotalTrackerStoryID());
        model.addAttribute("productionDeployedSHA", productionDeployedStory.getStorySHA());
        return "status";
    }
}
