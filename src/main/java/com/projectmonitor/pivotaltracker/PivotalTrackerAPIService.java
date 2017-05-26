package com.projectmonitor.pivotaltracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class PivotalTrackerAPIService implements PivotalTrackerAPI {

    private final RejectLabelService rejectLabelService;
    private final GetStoryService getStoryService;
    private final StoryStateService storyStateService;
    private final RemoveRejectLabelService removeRejectLabelService;

    @Autowired
    public PivotalTrackerAPIService(RejectLabelService rejectLabelService,
                                    GetStoryService getStoryService,
                                    StoryStateService storyStateService,
                                    RemoveRejectLabelService removeRejectLabelService) {
        this.rejectLabelService = rejectLabelService;
        this.getStoryService = getStoryService;
        this.storyStateService = storyStateService;
        this.removeRejectLabelService = removeRejectLabelService;
    }

    @Override
    public void rejectStory(String storyID) {
        storyStateService.setState(storyID, "rejected");
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

    @Override
    public void finishStory(String pivotalTrackerStoryID) {
        storyStateService.setState(pivotalTrackerStoryID, "finished");
    }
}
