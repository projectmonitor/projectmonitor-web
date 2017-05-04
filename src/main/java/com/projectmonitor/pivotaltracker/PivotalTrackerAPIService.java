package com.projectmonitor.pivotaltracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PivotalTrackerAPIService implements PivotalTrackerAPI {

    private final RejectLabelService rejectLabelService;
    private final GetStoryService getStoryService;
    private final RejectStoryService rejectStoryService;

    @Autowired
    public PivotalTrackerAPIService(RejectLabelService rejectLabelService,
                                    GetStoryService getStoryService,
                                    RejectStoryService rejectStoryService) {
        this.rejectLabelService = rejectLabelService;
        this.getStoryService = getStoryService;
        this.rejectStoryService = rejectStoryService;
    }

    @Override
    public void rejectStory(String storyID) {
        rejectStoryService.execute(storyID);
    }

    @Override
    public PivotalTrackerStory getStory(String pivotalTrackerStoryID) {
        return getStoryService.load(pivotalTrackerStoryID);
    }

    @Override
    public void addRejectLabel(String storyID) {
        rejectLabelService.add(storyID);
    }
}
