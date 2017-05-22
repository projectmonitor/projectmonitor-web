package com.projectmonitor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@PropertySource("classpath:tracker.properties")
@Component
public class DeployedVersionInfoContributor implements InfoContributor {
    @Value("${pivotalTrackerStoryID}")
    private String pivotalTrackerStoryID;

    @Value("${storySha}")
    private String storySHA;

    @Override
    public void contribute(Info.Builder builder) {
        final LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("pivotalTrackerStoryID", pivotalTrackerStoryID);
        map.put("storySHA", storySHA);
        builder.withDetails(map);
    }
}
