package com.projectmonitor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CIResponse {
    private String result;

    public String getResult() {
        return result;
    }
}
