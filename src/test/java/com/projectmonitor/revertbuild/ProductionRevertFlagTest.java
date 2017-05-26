package com.projectmonitor.revertbuild;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductionRevertFlagTest {

    private ProductionRevertFlag subject;

    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private BoundValueOperations<String, String> boundValueOperations;

    @Before
    public void setUp(){
        subject = new ProductionRevertFlag(redisTemplate);
    }

    @Test
    public void set_setsTheProductionRevertFlag() throws Exception {
        when(redisTemplate.boundValueOps(ProductionRevertFlag.KEY)).thenReturn(boundValueOperations);
        subject.set();
        verify(redisTemplate).boundValueOps(ProductionRevertFlag.KEY);
        verify(boundValueOperations).set("");
    }

    @Test
    public void get_whenTheFlagIsSet_returnsTrue() throws Exception {
        when(redisTemplate.boundValueOps(ProductionRevertFlag.KEY)).thenReturn(boundValueOperations);
        when(boundValueOperations.get()).thenReturn("");
        assertThat(subject.get()).isTrue();
    }

    @Test
    public void get_whenTheFlagIsNotSet_returnsFalse() throws Exception {
        when(redisTemplate.boundValueOps(ProductionRevertFlag.KEY)).thenReturn(boundValueOperations);
        assertThat(subject.get()).isFalse();
    }

    @Test
    public void clear_deletesTheKeyOutOfRedis() throws Exception {
        subject.clear();
        verify(redisTemplate).delete(ProductionRevertFlag.KEY);
    }
}