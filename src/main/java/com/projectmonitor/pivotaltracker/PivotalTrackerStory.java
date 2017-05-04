package com.projectmonitor.pivotaltracker;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
@EqualsAndHashCode
public class PivotalTrackerStory {
    private String currentState;
    private boolean hasBeenRejected;
}





