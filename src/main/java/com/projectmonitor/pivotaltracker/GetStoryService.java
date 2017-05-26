package com.projectmonitor.pivotaltracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Component
class GetStoryService {

    private URLGenerator urlGenerator;
    private RestTemplate restTemplate;
    private RejectLabelService rejectLabelService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public GetStoryService(URLGenerator urlGenerator,
                           RestTemplate restTemplate,
                           RejectLabelService rejectLabelService) {
        this.urlGenerator = urlGenerator;
        this.restTemplate = restTemplate;
        this.rejectLabelService = rejectLabelService;
    }

    PivotalTrackerStory load(String pivotalTrackerStoryID){
        String storyURL = urlGenerator.generate(pivotalTrackerStoryID);
        PivotalTrackerStoryDTO storyDTO = restTemplate.getForObject(storyURL, PivotalTrackerStoryDTO.class);

        boolean hasBeenRejected = false;
        String rejectedStoryID = "";
        if (storyDTO.getLabels() != null) {
            for (PivotalTrackerLabel label : storyDTO.getLabels()) {
                if ("rejected".equals(label.getName())) {
                    hasBeenRejected = true;
                    rejectedStoryID = label.getId();
                }
            }
        }

        if (Objects.equals(storyDTO.getCurrentState(), "rejected") && !hasBeenRejected) {
            rejectLabelService.add(pivotalTrackerStoryID);
            hasBeenRejected = true;
        }

        PivotalTrackerStory story = PivotalTrackerStory.builder()
                .currentState(storyDTO.getCurrentState())
                .hasBeenRejected(hasBeenRejected)
                .rejectedLabelID(rejectedStoryID)
                .build();

        return story;
    }
}
