package com.projectmonitor.jenkins;

import com.projectmonitor.deploypipeline.Deploy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class JenkinsRevertStoryAcceptance {

    private JenkinsJobAPI jenkinsJobAPI;
    private CIJobConfiguration ciJobConfiguration;
    private JenkinsJobPoller jenkinsJobPoller;

    @Autowired
    JenkinsRevertStoryAcceptance(JenkinsJobAPI jenkinsJobAPI,
                                 CIJobConfiguration ciJobConfiguration,
                                 JenkinsJobPoller jenkinsJobPoller) {
        this.jenkinsJobAPI = jenkinsJobAPI;
        this.ciJobConfiguration = ciJobConfiguration;
        this.jenkinsJobPoller = jenkinsJobPoller;
    }

    void execute(Deploy theDeploy) {
        String jobURL = ciJobConfiguration.getRevertStoryAcceptanceURL()
                + theDeploy.getSha() + "&STORY_ID=" + theDeploy.getStoryID();
        jenkinsJobAPI.triggerJob(jobURL);

        jenkinsJobPoller.execute(ciJobConfiguration.getRevertStoryAcceptanceStatusURL());
    }
}
