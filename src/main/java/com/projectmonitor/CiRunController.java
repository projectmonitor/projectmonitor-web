package com.projectmonitor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/")
public class CiRunController {

    @Value("${ciUrl}")
    private String ciUrl;
    @Value("${storyAcceptanceUrl}")
    private String storyAcceptanceUrl;
    @Value("${productionUrl}")
    private String productionUrl;

    @RequestMapping(method = RequestMethod.GET)
    public String execute(Model model) {
        RestTemplate restTemplate = new RestTemplate();

        CIResponse ciResponse = new CIResponse();
        try {
            ciResponse = restTemplate.getForObject(
                    ciUrl + "/job/TestProject CI/lastCompletedBuild/api/json",
                    CIResponse.class
            );
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            ciResponse.setResult("CI is not responding");
        }

        CIResponse storyAcceptanceDeployResponse = new CIResponse();
        try {
            storyAcceptanceDeployResponse = restTemplate.getForObject(
                    ciUrl + "/job/TestProject to SA/lastCompletedBuild/api/json",
                    CIResponse.class
            );
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            storyAcceptanceDeployResponse.setResult("Story Acceptance Deploy Job is not responding");
        }

        CIResponse productionDeployResponse = new CIResponse();
        try {
            productionDeployResponse = restTemplate.getForObject(
                    ciUrl + "/job/TestProject to Production/lastCompletedBuild/api/json",
                    CIResponse.class
            );
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            productionDeployResponse.setResult("Production Deploy Job is not responding");
        }

        DeployedAppInfo storyAcceptanceDeployedStory = new DeployedAppInfo();
        try {
            storyAcceptanceDeployedStory = restTemplate.getForObject(
                    storyAcceptanceUrl + "info",
                    DeployedAppInfo.class
            );
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            storyAcceptanceDeployedStory.setPivotalTrackerStoryID("Story Acceptance is not responding");
        }

        DeployedAppInfo productionDeployedStory = new DeployedAppInfo();
        try {
            productionDeployedStory = restTemplate.getForObject(
                    productionUrl + "info",
                    DeployedAppInfo.class
            );
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            productionDeployedStory.setPivotalTrackerStoryID("Production is not responding");
        }

        model.addAttribute("status", ciResponse.getResult());
        model.addAttribute("storyAcceptanceDeployResponse", storyAcceptanceDeployResponse.getResult());
        model.addAttribute("productionDeployResponse", productionDeployResponse.getResult());
        model.addAttribute("storyAcceptanceDeployedStoryID", storyAcceptanceDeployedStory.getPivotalTrackerStoryID());
        model.addAttribute("storyAcceptanceDeployedSHA", storyAcceptanceDeployedStory.getStorySHA());
        model.addAttribute("productionDeployedStoryID", productionDeployedStory.getPivotalTrackerStoryID());
        model.addAttribute("productionDeployedSHA", productionDeployedStory.getStorySHA());
        return "status";
    }
}
