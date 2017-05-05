package com.projectmonitor.pivotaltracker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PivotalTrackerAPIServiceTest {

    private PivotalTrackerAPIService subject;

    @Mock
    private RejectLabelService rejectLabelService;
    @Mock
    private GetStoryService getStoryService;
    @Mock
    private RejectStoryService rejectStoryService;
    @Mock
    private RemoveRejectLabelService removeRejectLabelService;

    @Before
    public void setUp() {
        subject = new PivotalTrackerAPIService(rejectLabelService, getStoryService, rejectStoryService, removeRejectLabelService);
    }

    @Test
    public void rejectStory_delegatesToRejectStoryService() throws Exception {
        subject.rejectStory("55");
        Mockito.verify(rejectStoryService).execute("55");
    }

    @Test
    public void getStory_delegatesToGetStoryService() throws Exception {
        subject.getStory("55");
        Mockito.verify(getStoryService).load("55");
    }

    @Test
    public void addRejectLabel_delegatesToRejectLabelService() throws Exception {
        subject.addRejectLabel("55");
        Mockito.verify(rejectLabelService).add("55");
    }

    @Test
    public void removeRejectLabel_delegates() throws Exception {
        subject.removeRejectLabel("55");
        Mockito.verify(removeRejectLabelService).execute("55");
    }
}