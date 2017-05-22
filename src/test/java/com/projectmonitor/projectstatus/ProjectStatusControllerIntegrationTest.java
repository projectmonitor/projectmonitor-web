package com.projectmonitor.projectstatus;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
@SpringBootTest
@WebAppConfiguration
public class ProjectStatusControllerIntegrationTest {

    private MockMvc mvc;
    private ClientAndServer mockServer;
    @Autowired
    private WebApplicationContext wac;

    @Before
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(wac).build();
        mockServer = startClientAndServer(1090);

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
                                .withBody("{\"pivotalTrackerStoryID\": \"55555\", \"storySHA\": \"88\"}")
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
                                .withBody("{\"pivotalTrackerStoryID\": \"42\", \"storySHA\": \"21\"}")
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
                                .withBody("{\"result\": \"BANANAS\"}")
                );

        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/1/story/55555")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(new Header("Content-Type", "application/json"))
                                .withBody("{\"current_state\": \"delivered\"}")
                );
    }

    @After
    public void tearDown() {
        mockServer.stop();
    }

    @Test
    public void whenCiResponds_wePrintTheBuildStatus() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("NOT_A_SUCCESS")))
        ;
    }

    @Test
    public void displaysAppInfoOfTheAppDeployedOnStoryAcceptance() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Story Currently Deployed in Story Acceptance:")))
                .andExpect(content().string(containsString("55555")))
                .andExpect(content().string(containsString("SHA Currently Deployed in Story Acceptance:")))
                .andExpect(content().string(containsString("88")))
        ;
    }

    @Test
    public void displaysAppInfoOfTheAppDeployedInProduction() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Story Currently Deployed in Production:")))
                .andExpect(content().string(containsString("42")))
                .andExpect(content().string(containsString("SHA Currently Deployed in Production:")))
                .andExpect(content().string(containsString("21")))
        ;
    }

    @Test
    public void displaysTheStateOfTheLastSAAndProdDeploy() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Last Deploy to Story Acceptance was: NOT_A_SUCCESS")))
                .andExpect(content().string(containsString("Last Deploy to Production was: BANANAS")))
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

    @Test
    public void whenStoryAcceptanceIsAccepted_displaysMessage() throws Exception {
        mockServer.clear(request().withMethod("GET").withPath("/1/story/55555"));
        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/1/story/55555")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(new Header("Content-Type", "application/json"))
                                .withBody("{\"current_state\": \"accepted\"}")
                );

        mvc.perform(MockMvcRequestBuilders
                .get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Story deployed in acceptance has been accepted!")))
        ;
    }

    @Test
    public void whenStoryAcceptanceHasARejectedStory_displaysMessage() throws Exception {
        mockServer.clear(request().withMethod("GET").withPath("/1/story/55555"));
        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/1/story/55555")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(new Header("Content-Type", "application/json"))
                                .withBody("{\"current_state\": \"rejected\"}")
                );

        HttpRequest request = new HttpRequest()
                .withMethod("POST")
                .withPath("/1/story/55555/labels")
                .withHeader("Content-Type", "application/json")
                .withHeader("X-TrackerToken", "some-tracker-token")
                .withHeader("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE)
                .withBody("{\"name\":\"rejected\"}");

        mockServer
                .when(request)
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(new Header("Content-Type", "application/json"))
                                .withBody("    {" +
                                        "\"created_at\": \"2017-04-25T12:00:00Z\"," +
                                        "\"id\": 5100," +
                                        "\"kind\": \"label\"," +
                                        "\"name\": \"rejected\"," +
                                        "\"project_id\": 2005," +
                                        "\"updated_at\": \"2017-04-25T12:00:00Z\"}")
                );

        mvc.perform(MockMvcRequestBuilders
                .get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Story deployed in acceptance has been rejected!")))
        ;
    }

    @Test
    public void whenStoryInAcceptanceHasBeenRejected_butHasBeenRestarted_displaysRejectedStatus() throws Exception {
        mockServer.clear(request().withMethod("GET").withPath("/1/story/55555"));
        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/1/story/55555")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(new Header("Content-Type", "application/json"))
                                .withBody("{\"current_state\": \"started\", \"labels\":[{\"name\":\"rejected\"}]}")
                );

        HttpRequest request = new HttpRequest()
                .withMethod("POST")
                .withPath("/1/story/55555/labels")
                .withHeader("Content-Type", "application/json")
                .withHeader("X-TrackerToken", "some-tracker-token")
                .withHeader("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE)
                .withBody("{\"name\":\"rejected\"}");

        mockServer
                .when(request)
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(new Header("Content-Type", "application/json"))
                                .withBody("    {" +
                                        "\"created_at\": \"2017-04-25T12:00:00Z\"," +
                                        "\"id\": 5100," +
                                        "\"kind\": \"label\"," +
                                        "\"name\": \"rejected\"," +
                                        "\"project_id\": 2005," +
                                        "\"updated_at\": \"2017-04-25T12:00:00Z\"}")
                );

        mvc.perform(MockMvcRequestBuilders
                .get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Story deployed in acceptance has been rejected!")))
        ;
    }

    @Test
    public void whenStoryAcceptanceStoryAwaitingDecision_displaysMessage() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Story deployed awaiting decision.")))
        ;
    }
}