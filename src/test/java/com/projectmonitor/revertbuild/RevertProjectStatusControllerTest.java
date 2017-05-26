package com.projectmonitor.revertbuild;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(RevertProjectStatusController.class)
public class RevertProjectStatusControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ProductionRevertFlag productionRevertFlag;

    @MockBean
    private ProductionRevertTask productionRevertTask;

    @Test
    public void post_kicksOffJenkinsRevertJob_redirectsToHomePage() throws Exception {
        this.mvc.perform(post("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("location", "/"))
        ;

        verify(productionRevertFlag).set();
        verify(productionRevertTask).start();
    }
}