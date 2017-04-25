package com.projectmonitor.productionrelease;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class PCFProductionDeployerTest {

    PCFProductionDeployer subject;

    @Mock
    RestTemplate productionReleaseRestTemplate;

    @Before
    public void setUp(){
        subject = new PCFProductionDeployer(productionReleaseRestTemplate);
    }

    @Test
    public void push_kicksOffProductionDeployJob_withShaFromAcceptance() throws Exception {
        String ciURL = "http://localhost:8080/job/TestProject to Production/buildWithParameters?SHA_TO_DEPLOY=blahblahSHA";

        subject.push("blahblahSHA");
        Mockito.verify(productionReleaseRestTemplate).getForObject(ciURL, Object.class);
    }

}