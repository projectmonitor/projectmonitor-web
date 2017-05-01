package com.projectmonitor;

import com.projectmonitor.productionrelease.JenkinsRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private JenkinsRestTemplate jenkinsRestTemplate;

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public CiRunController(CIJobConfiguration ciJobConfiguration,
                           ApplicationConfiguration applicationConfiguration,
                           JenkinsRestTemplate jenkinsRestTemplate) {
        this.ciJobConfiguration = ciJobConfiguration;
        this.applicationConfiguration = applicationConfiguration;
        this.jenkinsRestTemplate = jenkinsRestTemplate;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String execute(Model model) {

        String aura = "normal";
        jenkinsRestTemplate.addAuthentication(ciJobConfiguration.getCiUsername(), ciJobConfiguration.getCiPassword());

        CIResponse ciResponse = new CIResponse();
        try {
            ciResponse = jenkinsRestTemplate.getForObject(
                    ciJobConfiguration.getCiLastCompletedBuildURL(),
                    CIResponse.class
            );
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            ciResponse.setResult("CI is not responding");
            aura = "dependencyDown";
        }

        CIResponse storyAcceptanceDeployResponse = new CIResponse();
        try {
            storyAcceptanceDeployResponse = jenkinsRestTemplate.getForObject(
                    ciJobConfiguration.getStoryAcceptanceDeployJobLastStatusURL(),
                    CIResponse.class
            );
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            storyAcceptanceDeployResponse.setResult("Story Acceptance Deploy Job is not responding");
            aura = "dependencyDown";
        }

        if ("FAILURE".equals(storyAcceptanceDeployResponse.getResult())) {
            aura = "storyAcceptanceDeployFailed";
        }

        CIResponse productionDeployResponse = new CIResponse();
        try {
            productionDeployResponse = jenkinsRestTemplate.getForObject(
                    ciJobConfiguration.getProductionDeployJobLastStatusURL(),
                    CIResponse.class
            );
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            productionDeployResponse.setResult("Production Deploy Job is not responding");
            aura = "dependencyDown";
        }

        RestTemplate restTemplate = new RestTemplate();
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

        if ("FAILURE".equals(productionDeployResponse.getResult())) {
            aura = "productionDeployFailed";
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
