package com.projectmonitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/")
public class CiRunController {

    @Value("${storyAcceptanceUrl}")
    private String storyAcceptanceUrl;
    @Value("${productionUrl}")
    private String productionUrl;
    private CIJobConfiguration ciJobConfiguration;
    private ApplicationConfiguration applicationConfiguration;

    @Autowired
    public CiRunController(CIJobConfiguration ciJobConfiguration,
                           ApplicationConfiguration applicationConfiguration) {
        this.ciJobConfiguration = ciJobConfiguration;
        this.applicationConfiguration = applicationConfiguration;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String execute(Model model) {
        RestTemplate restTemplate = new RestTemplate();
        String aura = "normal";

        CIResponse ciResponse = new CIResponse();
        try {
            ciResponse = restTemplate.getForObject(
                    ciJobConfiguration.getCiLastCompletedBuildURL(),
                    CIResponse.class
            );
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            ciResponse.setResult("CI is not responding");
            aura = "dependencyDown";
        }

        CIResponse storyAcceptanceDeployResponse = new CIResponse();
        try {
            storyAcceptanceDeployResponse = restTemplate.getForObject(
                    ciJobConfiguration.getStoryAcceptanceDeployStatusURL(),
                    CIResponse.class
            );
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            storyAcceptanceDeployResponse.setResult("Story Acceptance Deploy Job is not responding");
            aura = "dependencyDown";
        }

        CIResponse productionDeployResponse = new CIResponse();
        try {
            productionDeployResponse = restTemplate.getForObject(
                    ciJobConfiguration.getProductionDeployJobLastStatusURL(),
                    CIResponse.class
            );
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            productionDeployResponse.setResult("Production Deploy Job is not responding");
            aura = "dependencyDown";
        }

        DeployedAppInfo storyAcceptanceDeployedStory = new DeployedAppInfo();
        try {
            storyAcceptanceDeployedStory = restTemplate.getForObject(
                    storyAcceptanceUrl,
                    DeployedAppInfo.class
            );
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            storyAcceptanceDeployedStory.setPivotalTrackerStoryID("Story Acceptance is not responding");
            aura = "storyAcceptanceDown";
        }

        DeployedAppInfo productionDeployedStory = new DeployedAppInfo();
        try {
            productionDeployedStory = restTemplate.getForObject(
                    productionUrl,
                    DeployedAppInfo.class
            );
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            productionDeployedStory.setPivotalTrackerStoryID("Production is not responding");
            aura = "productionDown";
        }

        if ("FAILURE".equals(storyAcceptanceDeployResponse.getResult())) {
            aura = "storyAcceptanceDeployFailed";
        }

        if ("FAILURE".equals(productionDeployResponse.getResult())) {
            aura = "productionDeployFailed";
        }

        model.addAttribute("status", ciResponse.getResult());
        model.addAttribute("githubUsername", applicationConfiguration.getGithubUsername());
        model.addAttribute("githubProjectName", applicationConfiguration.getGithubProjectName());
        model.addAttribute("backgroundColor", aura);
        model.addAttribute("storyAcceptanceDeployResponse", storyAcceptanceDeployResponse.getResult());
        model.addAttribute("productionDeployResponse", productionDeployResponse.getResult());
        model.addAttribute("storyAcceptanceDeployedStoryID", storyAcceptanceDeployedStory.getPivotalTrackerStoryID());
        model.addAttribute("storyAcceptanceDeployedSHA", storyAcceptanceDeployedStory.getStorySHA());
        model.addAttribute("productionDeployedStoryID", productionDeployedStory.getPivotalTrackerStoryID());
        model.addAttribute("productionDeployedSHA", productionDeployedStory.getStorySHA());
        return "status";
    }
}
