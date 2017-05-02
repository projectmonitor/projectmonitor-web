package com.projectmonitor.jenkins;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class CIJobConfiguration {

    private String ciLastCompletedBuildURL;

    private String storyAcceptanceDeployJobURL;
    private String storyAcceptanceDeployStatusURL;
    private String storyAcceptanceDeployJobLastStatusURL;

    private String productionDeployJobURL;
    private String productionDeployStatusURL;
    private String productionDeployJobLastStatusURL;
    
    private String ciUsername;
    private String ciPassword;


    public String getStoryAcceptanceDeployJobURL() {
        return storyAcceptanceDeployJobURL;
    }

    public void setStoryAcceptanceDeployJobURL(String storyAcceptanceDeployJobURL) {
        this.storyAcceptanceDeployJobURL = storyAcceptanceDeployJobURL;
    }

    public String getProductionDeployJobURL() {
        return productionDeployJobURL;
    }

    public void setProductionDeployJobURL(String productionDeployJobURL) {
        this.productionDeployJobURL = productionDeployJobURL;
    }

    public String getProductionDeployStatusURL() {
        return productionDeployStatusURL;
    }

    public void setProductionDeployStatusURL(String productionDeployStatusURL) {
        this.productionDeployStatusURL = productionDeployStatusURL;
    }

    public String getStoryAcceptanceDeployStatusURL() {
        return storyAcceptanceDeployStatusURL;
    }

    public void setStoryAcceptanceDeployStatusURL(String storyAcceptanceDeployStatusURL) {
        this.storyAcceptanceDeployStatusURL = storyAcceptanceDeployStatusURL;
    }

    public String getProductionDeployJobLastStatusURL() {
        return productionDeployJobLastStatusURL;
    }

    public void setProductionDeployJobLastStatusURL(String productionDeployJobLastStatusURL) {
        this.productionDeployJobLastStatusURL = productionDeployJobLastStatusURL;
    }

    public String getCiLastCompletedBuildURL() {
        return ciLastCompletedBuildURL;
    }

    public void setCiLastCompletedBuildURL(String ciLastCompletedBuildURL) {
        this.ciLastCompletedBuildURL = ciLastCompletedBuildURL;
    }

    public String getCiPassword() {
        return ciPassword;
    }

    public void setCiPassword(String ciPassword) {
        this.ciPassword = ciPassword;
    }

    public String getCiUsername() {
        return ciUsername;
    }

    public void setCiUsername(String ciUsername) {
        this.ciUsername = ciUsername;
    }

    public String getStoryAcceptanceDeployJobLastStatusURL() {
        return storyAcceptanceDeployJobLastStatusURL;
    }

    public void setStoryAcceptanceDeployJobLastStatusURL(String storyAcceptanceDeployJobLastStatusURL) {
        this.storyAcceptanceDeployJobLastStatusURL = storyAcceptanceDeployJobLastStatusURL;
    }
}
