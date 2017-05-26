package com.projectmonitor.deploys;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@EqualsAndHashCode
@ToString
@Getter
public class Deploy {

    private String sha;
    private String storyID;
}
