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
    }

    @After
    public void stopProxy() {
        mockServer.stop();
    }

    @Test
    public void whenCiFails_returnsFailedStatus() throws Exception {
        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/repos/projectmonitor/projectmonitor-web/branches/master")
                                .withHeaders(
                                        new Header("User-Agent", "us"),
                                        new Header("Accept", "application/vnd.travis-ci.2+json")
                                )
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(new Header("Content-Type", "application/json"))
                                .withBody("{\"branch\": {\"state\": \"failed\"}}")
                );

        mvc.perform(MockMvcRequestBuilders
                .get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("failed")));
    }

    @Test
    public void whenCiPasses_returnsPassesStatus() throws Exception {
        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/repos/projectmonitor/projectmonitor-web/branches/master")
                                .withHeaders(
                                        new Header("User-Agent", "us"),
                                        new Header("Accept", "application/vnd.travis-ci.2+json")
                                )
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(new Header("Content-Type", "application/json"))
                                .withBody("{\"branch\": {\"state\": \"passed\"}}")
                );

        mvc.perform(MockMvcRequestBuilders
                .get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("passed")));
    }

    @Test
    public void displaysTheTrackerStoryNumberDeployedOnStoryAcceptance() throws Exception {
        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/repos/projectmonitor/projectmonitor-web/branches/master")
                                .withHeaders(
                                        new Header("User-Agent", "us"),
                                        new Header("Accept", "application/vnd.travis-ci.2+json")
                                )
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(new Header("Content-Type", "application/json"))
                                .withBody("{\"branch\": {\"state\": \"passed\"}}")
                );

        mvc.perform(MockMvcRequestBuilders
                .get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Story Currently Deployed on Story Acceptance: #55555")));
    }
}