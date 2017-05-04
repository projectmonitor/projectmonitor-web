package com.projectmonitor.pivotaltracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Component
class GetStoryService {

    private URLGenerator urlGenerator;
    private RestTemplate productionReleaseRestTemplate;
    private RejectLabelService rejectLabelService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public GetStoryService(URLGenerator urlGenerator,
                           RestTemplate productionReleaseRestTemplate,
                           RejectLabelService rejectLabelService) {
        this.urlGenerator = urlGenerator;
        this.productionReleaseRestTemplate = productionReleaseRestTemplate;
        this.rejectLabelService = rejectLabelService;
    }

    PivotalTrackerStory load(String pivotalTrackerStoryID){
        String storyURL = urlGenerator.generate(pivotalTrackerStoryID);
        PivotalTrackerStoryDTO storyDTO = productionReleaseRestTemplate.getForObject(storyURL, PivotalTrackerStoryDTO.class);

        boolean hasBeenRejected = false;

        if (storyDTO.getLabels() != null) {
            for (PivotalTrackerLabel label : storyDTO.getLabels()) {
                if ("rejected".equals(label.getName())) {
                    hasBeenRejected = true;
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
                .build();

        logger.info("State of story: {}", story.getCurrentState());
        return story;
    }
}
