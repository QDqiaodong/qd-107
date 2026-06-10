package com.sport.checkin.common;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class RedisLock {

    private final RedisTemplate<String, Object> redisTemplate;

    private final ThreadLocal<String> lockValue = new ThreadLocal<>();

    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else " +
                    "return 0 " +
                    "end";

    public RedisLock(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryLock(String key, long expireTime, TimeUnit timeUnit) {
        String value = UUID.randomUUID().toString();
        lockValue.set(value);
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(key, value, expireTime, timeUnit);
        return Boolean.TRUE.equals(result);
    }

    public boolean tryLock(String key, long expireSeconds) {
        return tryLock(key, expireSeconds, TimeUnit.SECONDS);
    }

    public void unlock(String key) {
        String value = lockValue.get();
        if (value != null) {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(UNLOCK_SCRIPT);
            script.setResultType(Long.class);
            redisTemplate.execute(script, Collections.singletonList(key), value);
            lockValue.remove();
        }
    }
}
