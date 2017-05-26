package com.projectmonitor.deploypipeline;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductionDeployHistoryTest {
    private ProductionDeployHistory subject;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private BoundListOperations<String, String> boundListOperations;

    @Before
    public void setUp() {
        subject = new ProductionDeployHistory(redisTemplate);
    }

    @Test
    public void push_addsAnEntryToTheHeadOfTheQueue() throws Exception {
        when(redisTemplate.boundListOps(ProductionDeployHistory.KEY))
                .thenReturn(boundListOperations);

        subject.push("bobLobLawSha", "theStoryID");
        verify(boundListOperations).leftPush("bobLobLawSha-theStoryID");
    }

    @Test
    public void getLastDeploy_returnsTheDeployFromTheTopOfTheQueue() throws Exception {
        when(redisTemplate.boundListOps(ProductionDeployHistory.KEY))
                .thenReturn((boundListOperations));
        when(boundListOperations.index(0)).thenReturn("shaForBlahs-theLastDeploy");
        Deploy theLastDeploy = Deploy.builder()
                .storyID("theLastDeploy")
                .sha("shaForBlahs")
                .build();
        assertThat(subject.getLastDeploy()).isEqualTo(theLastDeploy);
        verify(boundListOperations).index(0);
    }

    @Test
    public void pop_removesTheDeployAtTheTopOfTheQueue() throws Exception {
        when(redisTemplate.boundListOps(ProductionDeployHistory.KEY))
                .thenReturn((boundListOperations));
        when(boundListOperations.leftPop()).thenReturn("theSha-theStoryID");

        subject.pop();
        verify(boundListOperations).leftPop();
    }

    @Test
    public void getLastDeploy_returnsNullWhenTheQueueIsEmpty() throws Exception {
        when(redisTemplate.boundListOps(ProductionDeployHistory.KEY))
                .thenReturn((boundListOperations));
        when(boundListOperations.index(0)).thenReturn(null);
        assertThat(subject.getLastDeploy()).isEqualTo(null);
    }

    @Test
    public void getGetPreviousDeploy_returnsNextToLastDeploy() throws Exception {
        when(redisTemplate.boundListOps(ProductionDeployHistory.KEY))
                .thenReturn((boundListOperations));
        when(boundListOperations.index(1)).thenReturn("theSha-aDeploy");
        Deploy theDeployPrevious = Deploy.builder()
                .storyID("aDeploy")
                .sha("theSha")
                .build();
        assertThat(subject.getPreviousDeploy()).isEqualTo(theDeployPrevious);
        verify(boundListOperations).index(1);
    }

    @Test
    public void getPreviousDeploy_returnsNullWhenIndexNotBigEnough() throws Exception {
        when(redisTemplate.boundListOps(ProductionDeployHistory.KEY))
                .thenReturn((boundListOperations));
        when(boundListOperations.index(1)).thenReturn(null);
        assertThat(subject.getPreviousDeploy()).isEqualTo(null);
    }
}