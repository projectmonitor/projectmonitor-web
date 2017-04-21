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

        CIResponse response = new CIResponse();
        try {
            response = restTemplate.getForObject(
                    ciUrl + "/job/TestProject CI/lastCompletedBuild/api/json",
                    CIResponse.class
            );
        } catch(org.springframework.web.client.HttpClientErrorException exception){
            response.setResult("CI is not responding");
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

        model.addAttribute("status", response.getResult());
        model.addAttribute("storyAcceptanceDeployedStoryID", storyAcceptanceDeployedStory.getPivotalTrackerStoryID());
        model.addAttribute("storyAcceptanceDeployedSHA", storyAcceptanceDeployedStory.getStorySHA());
        model.addAttribute("productionDeployedStoryID", productionDeployedStory.getPivotalTrackerStoryID());
        model.addAttribute("productionDeployedSHA", productionDeployedStory.getStorySHA());
        return "status";
    }
}
