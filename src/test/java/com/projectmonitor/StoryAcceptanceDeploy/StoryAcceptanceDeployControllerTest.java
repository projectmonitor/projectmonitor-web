package com.projectmonitor.StoryAcceptanceDeploy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(StoryAcceptanceDeployController.class)
public class StoryAcceptanceDeployControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    StoryAcceptanceQueue storyAcceptanceQueue;

    @Test
    public void put_addsToTheBuildList() throws Exception {
        this.mvc.perform(put("/storyAcceptanceDeploy/blahblahSHA-blahblahStory"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sha", is("blahblahSHA")))
                .andExpect(jsonPath("$.storyID", is("blahblahStory")))
        ;

        Mockito.verify(storyAcceptanceQueue).push("blahblahSHA-blahblahStory");
    }
}