package com.projectmonitor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CIResponse {
    private Branch branch;

    public Branch getBranch() {
        return branch;
    }
}
