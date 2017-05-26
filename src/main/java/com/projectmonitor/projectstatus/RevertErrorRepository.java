package com.projectmonitor.projectstatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
class RevertErrorRepository {

    private RedisTemplate<String, String> redisTemplate;

    static final String KEY = "PRODUCTION_REVERT_ERROR_MESSAGE";

    @Autowired
    RevertErrorRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    void save(String theCause) {
        redisTemplate.boundValueOps(KEY).set(theCause);
    }
}
