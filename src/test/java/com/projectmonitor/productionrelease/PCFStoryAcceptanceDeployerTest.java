package com.projectmonitor.productionrelease;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;

public class PCFStoryAcceptanceDeployerTest {

    PCFStoryAcceptanceDeployer subject;

    @Before
    public void setUp(){
        subject = new PCFStoryAcceptanceDeployer();
    }

    @Test
    public void push_triggersTheJenkinsSADeployJob_withTheNextBuildThatHasPassedCI() throws Exception {
        // Currently SA deploy script expects SHA to deploy
        // it checks if that sha is the top commit on the branch
        // and that the story for the branch is finished
        // then merges with master if no conflicts, re runs tests and pushes if they pass
        // so triggering a deploy of something not ready will be a non op to SA env


        // look at tracker, get all stories in finished state
        // curl -X GET -H "X-TrackerToken: $TOKEN" "https://www.pivotaltracker.com/services/v5/projects/$PROJECT_ID/search?query=state:finished"
        // gives you all stories in finished state (has #completes), with story numbers obvi
        // and has updated at, so you can determine commit order (kinda roughly)
        // how to determine if it passed CI on remote branch before

        // what happens if tests fail after merge? reject story with note? -- jenkins script should own this

        subject.push();
        fail();
    }
}