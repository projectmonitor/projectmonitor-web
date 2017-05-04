package com.projectmonitor.pivotaltracker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.when;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpResponse.response;

@RunWith(MockitoJUnitRunner.class)
public class RejectLabelServiceTest {

    private RejectLabelService subject;

    @Mock
    private URLGenerator urlGenerator;
    private ClientAndServer mockServer;

    @Before
    public void setUp() {
        PivotalTrackerStoryConfiguration pivotalTrackerStoryConfiguration = new PivotalTrackerStoryConfiguration();
        pivotalTrackerStoryConfiguration.setPivotalTrackerToken("some-tracker-token");
        when(urlGenerator.generate("55")).thenReturn("http://localhost:1090/services/v5/projects/2005/stories/55");

        mockServer = startClientAndServer(1090);

        subject  = new RejectLabelService(urlGenerator, pivotalTrackerStoryConfiguration, new RestTemplate());
    }

    @After
    public void stopProxy() {
        mockServer.stop();
    }

    @Test
    public void addRejectLabel_addsARejectLabelViaTrackerApi() throws Exception {
        HttpRequest request = new HttpRequest()
                .withMethod("POST")
                .withPath("/services/v5/projects/2005/stories/55/labels")
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


        subject.add("55");
        mockServer.verify(request);
    }

}