package com.projectmonitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.mockserver.MockServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
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
                                .withBody("{\"pivotalTrackerStoryID\": \"#55555\", \"storySHA\": \"88\"}")
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
                                .withBody("{\"pivotalTrackerStoryID\": \"#42\", \"storySHA\": \"21\"}")
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

        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/job/TestProject%20to%20SA/lastCompletedBuild/api/json")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(new Header("Content-Type", "application/json"))
                                .withBody("{\"result\": \"NOT_A_SUCCESS\"}")
                );

        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/job/TestProject%20to%20Production/lastCompletedBuild/api/json")
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
    public void displaysAppInfoOfTheAppDeployedOnStoryAcceptance() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Story Currently Deployed on Story Acceptance: #55555")))
                .andExpect(content().string(containsString("SHA Currently Deployed on Story Acceptance: 88")))
        ;
    }

    @Test
    public void displaysAppInfoOfTheAppDeployedInProduction() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Story Currently Deployed in Production: #42")))
                .andExpect(content().string(containsString("SHA Currently Deployed in Production: 21")))
        ;
    }

    @Test
    public void displaysTheStateOfTheLastSAAndProdDeploy() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Last Deploy to Story Acceptance was: NOT_A_SUCCESS")))
                .andExpect(content().string(containsString("Last Deploy to Production was: NOT_A_SUCCESS")))
        ;
    }

    @Test
    public void whenProductionDoesNotRespond_displaysThatTheEnvironmentIsDown() throws Exception {
        mockServer.clear(request().withMethod("GET").withPath("/prod/info"));

        mvc.perform(MockMvcRequestBuilders
                .get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Production is not responding")))
        ;
    }

    @Test
    public void whenStoryAcceptanceDoesNotRespond_displaysThatTheEnvironmentIsDown() throws Exception {
        mockServer.clear(request().withMethod("GET").withPath("/info"));

        mvc.perform(MockMvcRequestBuilders
                .get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Story Acceptance is not responding")))
        ;
    }

    @Test
    public void whenCIDoesNotRespond_displaysThatTheEnvironmentIsDown() throws Exception {
        mockServer.clear(request().withMethod("GET").withPath("/job/TestProject%20CI/lastCompletedBuild/api/json"));

        mvc.perform(MockMvcRequestBuilders
                .get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CI is not responding")))
        ;
    }

    @Test
    public void whenDeployJobsFail_displaysThatTheJobWasUnavailable() throws Exception {
        mockServer.clear(request().withMethod("GET").withPath("/job/TestProject%20to%20SA/lastCompletedBuild/api/json"));
        mockServer.clear(request().withMethod("GET").withPath("/job/TestProject%20to%20Production/lastCompletedBuild/api/json"));

        mvc.perform(MockMvcRequestBuilders
                .get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Story Acceptance Deploy Job is not responding")))
                .andExpect(content().string(containsString("Production Deploy Job is not responding")))
        ;
    }

}