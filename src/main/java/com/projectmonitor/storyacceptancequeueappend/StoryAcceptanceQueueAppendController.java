package com.projectmonitor.storyacceptancequeueappend;

import com.projectmonitor.deploys.Deploy;
import com.projectmonitor.deploys.StoryAcceptanceQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class StoryAcceptanceQueueAppendController {

    private StoryAcceptanceQueue storyAcceptanceQueue;

    @Autowired
    public StoryAcceptanceQueueAppendController(StoryAcceptanceQueue storyAcceptanceQueue) {
        this.storyAcceptanceQueue = storyAcceptanceQueue;
    }

    @RequestMapping(value = "/storyAcceptanceDeploy/{shaValue}-{storyID}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.CREATED)
    public Deploy put(@PathVariable String shaValue, @PathVariable String storyID){
        Deploy deploy = Deploy.builder()
                .sha(shaValue)
                .storyID(storyID)
                .build();

        storyAcceptanceQueue.push(shaValue, storyID);
        return deploy;
    }
}
