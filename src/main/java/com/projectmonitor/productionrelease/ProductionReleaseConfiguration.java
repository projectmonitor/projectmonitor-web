package com.projectmonitor.productionrelease;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableAsync
@EnableScheduling
public class ProductionReleaseConfiguration {

    @Bean(name = "productionReleaseRestTemplate")
    public RestTemplate getProductionReleaseRestTemplate() {
        return new RestTemplate();
    }

}
