package com.projectmonitor.projectstatus;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.projectmonitor.deploypipeline.ProductionDeployHistory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebClientBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@WebAppConfiguration
public class RevertDeployIntegrationTest {

    private MockMvc mvc;
    private ClientAndServer mockServer;
    @Autowired
    private WebApplicationContext wac;
    private WebClient webClient;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @Before
    public void setUp() throws Exception {
        webClient = MockMvcWebClientBuilder
                .webAppContextSetup(wac)
                .build();
        mvc = MockMvcBuilders.webAppContextSetup(wac).build();
        mockServer = startClientAndServer(1090);

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

        redisTemplate.delete(ProductionRevertFlag.KEY);
        redisTemplate.delete(ProductionDeployHistory.KEY);
    }

    @Test
    public void whenProdHasBeenDeployedTo_clickingTheRevertButtonSaysTheDeployIsReverting() throws Exception {
        HtmlPage page = webClient.getPage("http://localhost/");
        HtmlElement revertButton = page.getFirstByXPath("//input[@value=\"Revert Production Build\"]");

        HtmlPage pageWithButtonClicked = revertButton.click();

        HtmlSubmitInput button = pageWithButtonClicked.getFirstByXPath("//input[@value=\"Reverting Production Build\"]");
        assertThat(button.isDisabled()).isTrue();
    }
}