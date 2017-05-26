package com.projectmonitor.projectstatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductionRevertFlag {

    static final String KEY = "PRODUCTION_REVERT_FLAG";
    private RedisTemplate<String, String> redisTemplate;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    public ProductionRevertFlag(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set() {
        redisTemplate.boundValueOps(KEY).set("");
    }

    public boolean get() {
        return redisTemplate.boundValueOps(KEY).get() != null;
    }

    public void clear() {
        redisTemplate.delete(KEY);
    }
}
