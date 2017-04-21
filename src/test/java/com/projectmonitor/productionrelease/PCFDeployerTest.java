package com.projectmonitor.productionrelease;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class PCFDeployerTest {

    PCFDeployer subject;

    @Mock
    RestTemplate productionReleaseRestTemplate;

    @Before
    public void setUp(){
        subject = new PCFDeployer(productionReleaseRestTemplate);
    }

    @Test
    public void push_kicksOffProductionDeployJob() throws Exception {
        String ciURL = "http://localhost:8080/job/TestProject to Production/build";

        subject.push();
        Mockito.verify(productionReleaseRestTemplate).getForObject(ciURL, Object.class);
    }

}