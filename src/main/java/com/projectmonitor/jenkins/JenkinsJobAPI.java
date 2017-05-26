package com.projectmonitor.jenkins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component
public class JenkinsJobAPI {

    private RestTemplate restTemplate;
    private CIJobConfiguration ciJobConfiguration;

    @Autowired
    public JenkinsJobAPI(RestTemplate restTemplate,
                         CIJobConfiguration ciJobConfiguration) {
        this.restTemplate = restTemplate;
        this.ciJobConfiguration = ciJobConfiguration;
    }

    private void addAuthentication(String username, String password) {
        List<ClientHttpRequestInterceptor> interceptors = Collections
                .singletonList(new BasicAuthorizationInterceptor(
                        username, password));
        restTemplate.setRequestFactory(
                new InterceptingClientHttpRequestFactory(restTemplate.getRequestFactory(),
                        interceptors)
        );
    }

    public CIResponse loadJobStatus(String url) {
        addAuthentication(ciJobConfiguration.getCiUsername(), ciJobConfiguration.getCiPassword());
        return restTemplate.getForObject(
                url,
                CIResponse.class
        );
    }

    public void triggerJob(String url) throws RequestFailedException {
        addAuthentication(ciJobConfiguration.getCiUsername(), ciJobConfiguration.getCiPassword());
        try {
            restTemplate.postForEntity(
                    url,
                    null,
                    String.class);
        } catch (HttpStatusCodeException e)  // thrown by DefaultResponseErrorHandler
        {
            String message = "Trigger job request failed with status: " +
                    e.getStatusCode() + "response: \n" +
                    e.getResponseBodyAsString();
            throw new RequestFailedException(message, e);
        } catch (Exception e) {
            throw new RequestFailedException(e.getMessage(), e);
        }
    }
}
