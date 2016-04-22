package com.projectmonitor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.InfoEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.LinkedHashMap;

@PropertySource("classpath:tracker.properties")
@Configuration
public class InfoConfiguration {

    @Value("${pivotalTrackerStoryID}")
    private String pivotalTrackerStoryID;

    @Bean
    public InfoEndpoint infoEndpoint() {
        final LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("pivotalTrackerStoryID", pivotalTrackerStoryID);
        return new InfoEndpoint(map);
    }

}
