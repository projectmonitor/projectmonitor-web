package com.projectmonitor.deployjob;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableAsync
@EnableScheduling
public class DeployJobConfiguration {

    @Bean(name = "restTemplate")
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
