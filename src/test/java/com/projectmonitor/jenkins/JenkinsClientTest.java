package com.projectmonitor.jenkins;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@RunWith(MockitoJUnitRunner.class)
public class JenkinsClientTest {

    JenkinsClient subject;
    private ClientAndServer mockServer;
    RestTemplate restTemplate;
    CIJobConfiguration ciJobConfiguration;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        ciJobConfiguration = new CIJobConfiguration();
        ciJobConfiguration.setCiUsername("banana");
        ciJobConfiguration.setCiPassword("damage");
        restTemplate = new RestTemplate();
        subject = new JenkinsClient(restTemplate, ciJobConfiguration);

        mockServer = startClientAndServer(1090);
    }

    @After
    public void stopProxy() {
        mockServer.stop();
    }

    @Test
    public void loadJobStatus_addsAuthToOutBoundRequests() throws Exception {
        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/whut")
                                .withHeader("Authorization", "Basic YmFuYW5hOmRhbWFnZQ==")

                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(new Header("Content-Type", "application/json"))
                                .withBody("{\"result\": \"hello\"}")
                );

        assertThat(subject.loadJobStatus("http://localhost:1090/whut").getResult()).isEqualTo("hello");
    }

    @Test
    public void loadJobStatus_whenTheRequestFails_throwsAnException() throws Exception {
        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/whut")
                                .withHeader("Authorization", "Basic YmFuYW5hOmRhbWFnZQ==")

                )
                .respond(
                        response()
                                .withStatusCode(400)
                                .withHeaders(new Header("Content-Type", "application/json"))
                                .withBody("the error")
                );

        exception.expect(RequestFailedException.class);
        exception.expectMessage("Trigger job request failed with status: 400 response: the error");
        subject.loadJobStatus("http://localhost:1090/whut");
    }

    @Test
    public void triggerJob_addsAuth() throws Exception {
        HttpRequest request = request()
                .withMethod("POST")
                .withPath("/whut")
                .withHeader("Authorization", "Basic YmFuYW5hOmRhbWFnZQ==");

        mockServer
                .when(request)
                .respond(response().withStatusCode(201))
        ;

        subject.triggerJob("http://localhost:1090/whut");
        mockServer.verify(request);
    }

    @Test
    public void triggerJob_whenRequestHasAFailureStatusCode_throwsException() throws Exception {
        HttpRequest request = request()
                .withMethod("POST")
                .withPath("/whut")
                .withHeader("Authorization", "Basic YmFuYW5hOmRhbWFnZQ==");

        mockServer
                .when(request)
                .respond(
                        response()
                                .withStatusCode(400)
                                .withBody("the error")
                )
        ;

        exception.expect(RequestFailedException.class);
        exception.expectMessage("Trigger job request failed with status: 400 response: the error");
        subject.triggerJob("http://localhost:1090/whut");
    }
}