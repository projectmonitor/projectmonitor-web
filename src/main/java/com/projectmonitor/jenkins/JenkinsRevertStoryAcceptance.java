package com.projectmonitor.jenkins;

import com.projectmonitor.deploypipeline.Deploy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class JenkinsRevertStoryAcceptance {

    private JenkinsRestTemplate jenkinsRestTemplate;
    private CIJobConfiguration ciJobConfiguration;
    private JenkinsJobPoller jenkinsJobPoller;

    @Autowired
    JenkinsRevertStoryAcceptance(JenkinsRestTemplate jenkinsRestTemplate,
                                 CIJobConfiguration ciJobConfiguration,
                                 JenkinsJobPoller jenkinsJobPoller) {
        this.jenkinsRestTemplate = jenkinsRestTemplate;
        this.ciJobConfiguration = ciJobConfiguration;
        this.jenkinsJobPoller = jenkinsJobPoller;
    }

    void execute(Deploy theDeploy) {
        String jobURL = ciJobConfiguration.getRevertStoryAcceptanceURL()
                + theDeploy.getSha() + "&STORY_ID=" + theDeploy.getStoryID();
        jenkinsRestTemplate.triggerJob(jobURL);

        jenkinsJobPoller.execute(ciJobConfiguration.getRevertStoryAcceptanceStatusURL());
    }
}
