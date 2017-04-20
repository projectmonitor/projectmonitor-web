package com.projectmonitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ProjectmonitorWebApplication.class)
@WebAppConfiguration
public class CiRunControllerTest {

    private MockMvc mvc;
    private ClientAndServer mockServer;
    @Autowired
    private WebApplicationContext wac;

    @Before
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(wac).build();
        mockServer = startClientAndServer(1080);

        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/info")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(new Header("Content-Type", "application/json"))
                                .withBody("{\"pivotalTrackerStoryID\": \"#55555\"}")
                );

        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/prod/info")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(new Header("Content-Type", "application/json"))
                                .withBody("{\"pivotalTrackerStoryID\": \"#42\"}")
                );

        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/job/TestProject%20CI/lastCompletedBuild/api/json")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(new Header("Content-Type", "application/json"))
                                .withBody("{\"result\": \"NOT_A_SUCCESS\"}")
                );
    }

    @After
    public void stopProxy() {
        mockServer.stop();
    }

    @Test
    public void whenCiResponds_wePrintTheBuildStatus() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("NOT_A_SUCCESS")));
    }

    @Test
    public void displaysTheTrackerStoryNumberDeployedOnStoryAcceptance() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Story Currently Deployed on Story Acceptance: #55555")));
    }

    @Test
    public void displaysTheTrackerStoryNumberDeployedInProduction() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Story Currently Deployed in Production: #42")));
    }
}