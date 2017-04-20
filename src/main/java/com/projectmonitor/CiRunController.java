package com.projectmonitor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

    @RequestMapping(method = RequestMethod.GET)
    public String execute(Model model) {
        RestTemplate restTemplate = new RestTemplate();

        CIResponse response = restTemplate.getForObject(
                ciUrl + "/job/TestProject CI/lastCompletedBuild/api/json",
                CIResponse.class
        );

        DeployedTrackerStory projectMonitorTrackerStoryInfo = restTemplate.getForObject(
                storyAcceptanceUrl + "info",
                DeployedTrackerStory.class
        );

        model.addAttribute("status", response.getResult());
        model.addAttribute("pivotalTrackerStoryID", projectMonitorTrackerStoryInfo.getPivotalTrackerStoryID());
        return "status";
    }
}
