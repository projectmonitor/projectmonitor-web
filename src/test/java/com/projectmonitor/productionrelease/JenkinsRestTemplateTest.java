package com.projectmonitor.productionrelease;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@RunWith(MockitoJUnitRunner.class)
public class JenkinsRestTemplateTest {

    JenkinsRestTemplate subject;
    private ClientAndServer mockServer;

    @Before
    public void setUp() {
        subject = new JenkinsRestTemplate();

        mockServer = startClientAndServer(1090);
    }

    @Test
    public void addAuthentication_addsAuthToOutBoundRequests() throws Exception {
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
                                .withBody("hello")
                );

        subject.addAuthentication("banana", "damage");

        assertThat(subject.getForObject("http://localhost:1090/whut", String.class)).isEqualTo("hello");
    }
}