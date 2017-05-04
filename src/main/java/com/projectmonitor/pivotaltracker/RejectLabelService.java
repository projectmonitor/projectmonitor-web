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

import java.util.ArrayList;
import java.util.List;

@Component
class RejectLabelService {

    private URLGenerator urlGenerator;
    private PivotalTrackerStoryConfiguration pivotalTrackerStoryConfiguration;
    private RestTemplate productionReleaseRestTemplate;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public RejectLabelService(URLGenerator urlGenerator,
                              PivotalTrackerStoryConfiguration pivotalTrackerStoryConfiguration,
                              RestTemplate productionReleaseRestTemplate) {
        this.urlGenerator = urlGenerator;
        this.pivotalTrackerStoryConfiguration = pivotalTrackerStoryConfiguration;
        this.productionReleaseRestTemplate = productionReleaseRestTemplate;
    }

    void add(String storyID) {
        String storyURL = urlGenerator.generate(storyID) + "/labels";
        HttpHeaders headers = new HttpHeaders();
        headers.clear();
        headers.set("X-TrackerToken", pivotalTrackerStoryConfiguration.getPivotalTrackerToken());
        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        List<MediaType> acceptHeaders = new ArrayList<>();
        acceptHeaders.add(MediaType.APPLICATION_JSON_UTF8);
        headers.setAccept(acceptHeaders);

        PivotalTrackerLabel body = new PivotalTrackerLabel();
        body.setName("rejected");

        HttpEntity<PivotalTrackerLabel> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = productionReleaseRestTemplate.exchange(
                storyURL,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (response.getStatusCodeValue() > 399) {
            logger.error("Failed adding label to tracker Story: " + storyID);
        }
    }
}
