package com.projectmonitor.productionrelease;

import com.projectmonitor.ApplicationConfiguration;
import com.projectmonitor.DeployedAppInfo;
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
    private final PCFProductionDeployer pcfProductionDeployer;
    private final PCFStoryAcceptanceDeployer pcfStoryAcceptanceDeployer;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public CheckCurrentAcceptanceStoryStatusService(RestTemplate productionReleaseRestTemplate,
                                                    PivotalTrackerStoryConfiguration pivotalTrackerStoryConfiguration,
                                                    ApplicationConfiguration applicationConfiguration,
                                                    PCFProductionDeployer pcfProductionDeployer,
                                                    PCFStoryAcceptanceDeployer pcfStoryAcceptanceDeployer) {
        this.productionReleaseRestTemplate = productionReleaseRestTemplate;
        this.pivotalTrackerStoryConfiguration = pivotalTrackerStoryConfiguration;
        this.applicationConfiguration = applicationConfiguration;
        this.pcfProductionDeployer = pcfProductionDeployer;
        this.pcfStoryAcceptanceDeployer = pcfStoryAcceptanceDeployer;
    }

    @Scheduled(fixedDelay = 180000, initialDelay = 10000)
    public void execute() {
        logger.info("Job to determine if we should deploy to production kicking off...");

        try {
            DeployedAppInfo acceptanceStory = productionReleaseRestTemplate.getForObject(applicationConfiguration.getStoryAcceptanceUrl(), DeployedAppInfo.class);
            logger.info("Current story in acceptance {}", acceptanceStory.getPivotalTrackerStoryID());

            DeployedAppInfo productionStory = productionReleaseRestTemplate.getForObject(applicationConfiguration.getProductionUrl(), DeployedAppInfo.class);
            logger.info("Current story in production: {}", productionStory.getPivotalTrackerStoryID());

            String storyURL = generatePivotalTrackerUrl(acceptanceStory.getPivotalTrackerStoryID());

            PivotalTrackerStory story = productionReleaseRestTemplate.getForObject(storyURL, PivotalTrackerStory.class);
            logger.info("State of story currently in acceptance: {}", story.getCurrentState());

            if ("accepted".equals(story.getCurrentState())) {
                if(!acceptanceStory.getPivotalTrackerStoryID().equals(productionStory.getPivotalTrackerStoryID())){
                    if (pcfProductionDeployer.push(acceptanceStory.getStorySHA(), acceptanceStory.getPivotalTrackerStoryID())) {
                        pcfStoryAcceptanceDeployer.push();
                    }
                } else {
                    logger.info("Story in SA deployed already, looking for new build to deploy to SA");
                    pcfStoryAcceptanceDeployer.push();
                }
            } else {
                logger.info("Nothing to deploy at the moment...");
            }
        } catch (org.springframework.web.client.HttpClientErrorException exception) {
            logger.info("A web call to acceptance/production or tracker errored! {}", exception.getMessage());
        }
    }

    private String generatePivotalTrackerUrl(String pivotalTrackerStoryID) {
        String storyURL = pivotalTrackerStoryConfiguration.getPivotalTrackerStoryDetailsUrl();
        storyURL = storyURL.replace("{STORY_ID}", pivotalTrackerStoryID);
        storyURL = storyURL.replace("{TRACKER_PROJECT_ID}", pivotalTrackerStoryConfiguration.getTrackerProjectId());

        logger.info("Url of the current story in acceptance: {}", storyURL);
        return storyURL;
    }
}
