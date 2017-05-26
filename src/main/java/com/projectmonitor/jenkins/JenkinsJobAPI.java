package com.projectmonitor.jenkins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
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

    // TODO this class and the other API class, same concept?
    private void addAuthentication(String username, String password) {
        List<ClientHttpRequestInterceptor> interceptors = Collections
                .singletonList(new BasicAuthorizationInterceptor(
                        username, password));
        restTemplate.setRequestFactory(
                new InterceptingClientHttpRequestFactory(restTemplate.getRequestFactory(),
                        interceptors)
        );
    }

    public CIResponse loadJobStatus(String url) throws RequestFailedException {
        addAuthentication(ciJobConfiguration.getCiUsername(), ciJobConfiguration.getCiPassword());
        try {
            return restTemplate.getForObject(
                    url,
                    CIResponse.class
            );
        } catch (HttpStatusCodeException e) {
            String message = "Trigger job request failed with status: " +
                    e.getStatusCode() + " response: " +
                    e.getResponseBodyAsString();
            throw new RequestFailedException(message, e);
        } catch (Exception e) {
            throw new RequestFailedException(e.getMessage(), e);
        }
    }

    public void triggerJob(String url) throws RequestFailedException {
        addAuthentication(ciJobConfiguration.getCiUsername(), ciJobConfiguration.getCiPassword());
        try {
            restTemplate.postForEntity(
                    url,
                    null,
                    String.class);
        } catch (HttpStatusCodeException e) {
            String message = "Trigger job request failed with status: " +
                    e.getStatusCode() + " response: " +
                    e.getResponseBodyAsString();
            throw new RequestFailedException(message, e);
        } catch (Exception e) {
            throw new RequestFailedException(e.getMessage(), e);
        }
    }
}
