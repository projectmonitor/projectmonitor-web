package com.projectmonitor;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ProjectMonitorConfiguration {
    @Bean(name = "restTemplate")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
