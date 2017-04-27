package com.projectmonitor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class CIJobConfiguration {

    private String ciLastCompletedBuildURL;
    private String storyAcceptanceDeployJobURL;
    private String storyAcceptanceDeployStatusURL;
    private String productionDeployJobURL;
    private String productionDeployStatusURL;
    private String productionDeployJobLastStatusURL;

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
}
