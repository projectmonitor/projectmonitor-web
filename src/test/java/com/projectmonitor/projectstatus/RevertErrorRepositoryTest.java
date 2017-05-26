package com.projectmonitor.projectstatus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RevertErrorRepositoryTest {

    @InjectMocks
    private RevertErrorRepository subject;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private BoundValueOperations<String, String> boundValueOperations;

    @Before
    public void setUp() {
        when(redisTemplate.boundValueOps(RevertErrorRepository.KEY)).thenReturn(boundValueOperations);
    }

    @Test
    public void blah() throws Exception {
        subject.save("some error");
        verify(boundValueOperations).set("some error");
    }
}