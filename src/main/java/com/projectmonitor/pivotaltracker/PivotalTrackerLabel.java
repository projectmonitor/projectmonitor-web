package com.projectmonitor.pivotaltracker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
@Getter
@Setter
class PivotalTrackerLabel {
    private String name;
    private String id;
}
