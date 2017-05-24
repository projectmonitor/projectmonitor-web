package com.projectmonitor.pivotaltracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class PivotalTrackerAPIService implements PivotalTrackerAPI {

    private final RejectLabelService rejectLabelService;
    private final GetStoryService getStoryService;
    private final RejectStoryService rejectStoryService;
    private RemoveRejectLabelService removeRejectLabelService;

    @Autowired
    public PivotalTrackerAPIService(RejectLabelService rejectLabelService,
                                    GetStoryService getStoryService,
                                    RejectStoryService rejectStoryService,
                                    RemoveRejectLabelService removeRejectLabelService) {
        this.rejectLabelService = rejectLabelService;
        this.getStoryService = getStoryService;
        this.rejectStoryService = rejectStoryService;
        this.removeRejectLabelService = removeRejectLabelService;
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

    @Override
    public void removeRejectLabel(String storyID){
        removeRejectLabelService.execute(storyID);
    }
}
