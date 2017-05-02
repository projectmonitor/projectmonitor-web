package com.projectmonitor.StoryAcceptanceDeploy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StoryAcceptanceDeployController {

    private StoryAcceptanceQueue storyAcceptanceQueue;

    @Autowired
    public StoryAcceptanceDeployController(StoryAcceptanceQueue storyAcceptanceQueue) {
        this.storyAcceptanceQueue = storyAcceptanceQueue;
    }

    @RequestMapping(value = "/storyAcceptanceDeploy/{shaValue}-{storyID}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.CREATED)
    public Deploy put(@PathVariable String shaValue, @PathVariable String storyID){
        Deploy deploy = new Deploy();
        deploy.setSha(shaValue);
        deploy.setStoryID(storyID);
        storyAcceptanceQueue.push(shaValue, storyID);
        return deploy;
    }
}
