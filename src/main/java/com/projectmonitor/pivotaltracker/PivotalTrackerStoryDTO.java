package com.projectmonitor.pivotaltracker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
@Setter
@Getter
class PivotalTrackerStoryDTO {
    @JsonProperty(value = "current_state")
    private String currentState;

    private List<PivotalTrackerLabel> labels;
}
