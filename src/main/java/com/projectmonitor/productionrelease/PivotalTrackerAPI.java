package com.projectmonitor.productionrelease;

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
public class PivotalTrackerAPI {

    private final PivotalTrackerStoryConfiguration pivotalTrackerStoryConfiguration;
    private final RestTemplate productionReleaseRestTemplate;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public PivotalTrackerAPI(PivotalTrackerStoryConfiguration pivotalTrackerStoryConfiguration,
                             RestTemplate productionReleaseRestTemplate) {
        this.pivotalTrackerStoryConfiguration = pivotalTrackerStoryConfiguration;
        this.productionReleaseRestTemplate = productionReleaseRestTemplate;
    }

    void rejectStory(String storyID) {
        String storyURL = generatePivotalTrackerUrl(storyID);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-TrackerToken", pivotalTrackerStoryConfiguration.getPivotalTrackerToken());
        headers.set("Content-Type", "application/json");

        PivotalTrackerStory body = new PivotalTrackerStory();
        body.setCurrentState("rejected");

        HttpEntity<PivotalTrackerStory> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = productionReleaseRestTemplate.exchange(
                storyURL,
                HttpMethod.PUT,
                entity,
                String.class
        );

        if(response.getStatusCodeValue() > 399){
            logger.error("Failed marking tracker Story: " + storyID + " as rejected.");
        }
    }

    public PivotalTrackerStory getStory(String pivotalTrackerStoryID) {
        String storyURL = generatePivotalTrackerUrl(pivotalTrackerStoryID);
        PivotalTrackerStory story = productionReleaseRestTemplate.getForObject(storyURL, PivotalTrackerStory.class);
        logger.info("State of story: {}", story.getCurrentState());
        return story;
    }

    private String generatePivotalTrackerUrl(String pivotalTrackerStoryID) {
        String storyURL = pivotalTrackerStoryConfiguration.getPivotalTrackerStoryDetailsUrl();
        storyURL = storyURL.replace("{STORY_ID}", pivotalTrackerStoryID);
        storyURL = storyURL.replace("{TRACKER_PROJECT_ID}", pivotalTrackerStoryConfiguration.getTrackerProjectId());

        logger.info("Url of the current story: {}", storyURL);
        return storyURL;
    }
}
