package com.projectmonitor.pivotaltracker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
@Getter
@Setter
public class PivotalTrackerLabelSlim {

    private String name;
}
