package com.projectmonitor.productionrelease;

import com.projectmonitor.ApplicationConfiguration;
import com.projectmonitor.ProjectMonitorTrackerStoryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CheckCurrentAcceptanceStoryStatusService {

    private final RestTemplate productionReleaseRestTemplate;
    private final PivotalTrackerStoryConfiguration pivotalTrackerStoryConfiguration;
    private final ApplicationConfiguration applicationConfiguration;
    private final PCFDeployer pcfDeployer;
    private static final Logger logger = LoggerFactory.getLogger(CheckCurrentAcceptanceStoryStatusService.class);

    @Autowired
    public CheckCurrentAcceptanceStoryStatusService(RestTemplate productionReleaseRestTemplate,
                                                    PivotalTrackerStoryConfiguration pivotalTrackerStoryConfiguration,
                                                    ApplicationConfiguration applicationConfiguration,
                                                    PCFDeployer pcfDeployer) {
        this.productionReleaseRestTemplate = productionReleaseRestTemplate;
        this.pivotalTrackerStoryConfiguration = pivotalTrackerStoryConfiguration;
        this.applicationConfiguration = applicationConfiguration;
        this.pcfDeployer = pcfDeployer;
    }

    @Scheduled(fixedDelay = 180000)
    public void execute() {
        logger.info("Job to determine if we should deploy to production kicking off...");

        try {
            ProjectMonitorTrackerStoryInfo acceptanceStoryInfo = productionReleaseRestTemplate.getForObject(applicationConfiguration.getStoryAcceptanceUrl() + "info", ProjectMonitorTrackerStoryInfo.class);
            logger.info("Current story in acceptance {}", acceptanceStoryInfo.getPivotalTrackerStoryID());

            ProjectMonitorTrackerStoryInfo productionStoryInfo = productionReleaseRestTemplate.getForObject(applicationConfiguration.getProductionUrl() + "info", ProjectMonitorTrackerStoryInfo.class);
            logger.info("Current story in production: {}", productionStoryInfo.getPivotalTrackerStoryID());

            String storyURL = pivotalTrackerStoryConfiguration.getPivotalTrackerStoryDetailsUrl().replace("{STORY_ID}", acceptanceStoryInfo.getPivotalTrackerStoryID());
            logger.info("Url of the current story in acceptance: {}", storyURL);

            PivotalTrackerStory story = productionReleaseRestTemplate.getForObject(storyURL, PivotalTrackerStory.class);
            logger.info("State of story currently in acceptance: {}", story.getCurrentState());

            if ("accepted".equals(story.getCurrentState()) && acceptanceStoryInfo.getPivotalTrackerStoryID() != productionStoryInfo.getPivotalTrackerStoryID()) {
                pcfDeployer.push();
            } else {
                logger.info("Nothing to deploy at the moment...");
            }
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            logger.info("A web call to acceptance/production or tracker errored! {}", exception.getMessage());
        }
    }
}
