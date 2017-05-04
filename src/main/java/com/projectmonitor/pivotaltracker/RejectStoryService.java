package com.projectmonitor.pivotaltracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
class RejectStoryService {

    private URLGenerator urlGenerator;
    private PivotalTrackerStoryConfiguration pivotalTrackerStoryConfiguration;
    private RestTemplate productionReleaseRestTemplate;

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public RejectStoryService(URLGenerator urlGenerator,
                              PivotalTrackerStoryConfiguration pivotalTrackerStoryConfiguration,
                              RestTemplate productionReleaseRestTemplate) {
        this.urlGenerator = urlGenerator;
        this.pivotalTrackerStoryConfiguration = pivotalTrackerStoryConfiguration;
        this.productionReleaseRestTemplate = productionReleaseRestTemplate;
    }

    void execute(String storyID) {
        String storyURL = urlGenerator.generate(storyID);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-TrackerToken", pivotalTrackerStoryConfiguration.getPivotalTrackerToken());
        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        PivotalTrackerStoryDTO body = new PivotalTrackerStoryDTO();
        body.setCurrentState("rejected");

        HttpEntity<PivotalTrackerStoryDTO> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = productionReleaseRestTemplate.exchange(
                storyURL,
                HttpMethod.PUT,
                entity,
                String.class
        );

        if (response.getStatusCodeValue() > 399) {
            logger.error("Failed marking tracker Story: " + storyID + " as rejected.");
        }

    }
}
