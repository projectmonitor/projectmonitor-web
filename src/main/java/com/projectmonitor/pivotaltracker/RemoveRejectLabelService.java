package com.projectmonitor.pivotaltracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
class RemoveRejectLabelService {

    private URLGenerator urlGenerator;
    private PivotalTrackerStoryConfiguration pivotalTrackerStoryConfiguration;
    private RestTemplate restTemplate;
    private GetStoryService getStoryService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public RemoveRejectLabelService(URLGenerator urlGenerator,
                                    PivotalTrackerStoryConfiguration pivotalTrackerStoryConfiguration,
                                    RestTemplate restTemplate,
                                    GetStoryService getStoryService) {
        this.urlGenerator = urlGenerator;
        this.pivotalTrackerStoryConfiguration = pivotalTrackerStoryConfiguration;
        this.restTemplate = restTemplate;
        this.getStoryService = getStoryService;
    }

    public void execute(String storyID) {
        PivotalTrackerStory story = getStoryService.load(storyID);
        String url = urlGenerator.generate(storyID) + "/labels/" + story.getRejectedLabelID();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-TrackerToken", pivotalTrackerStoryConfiguration.getPivotalTrackerToken());
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        logger.info("Removing rejected label from story: {}", storyID);
        ResponseEntity response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);

        if(response.getStatusCodeValue() > 399){
            logger.error("failed removing label form story: {}. \n Error response: {}", storyID, response.toString());
        }

    }
}
