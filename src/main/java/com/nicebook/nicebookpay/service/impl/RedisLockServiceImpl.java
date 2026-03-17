package com.nicebook.nicebookpay.service.impl;


import com.nicebook.nicebookpay.service.LockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisLockServiceImpl implements LockService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean tryLock(String key, String value, int seconds) {
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, value, seconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    @Override
    public boolean unlock(String key, String value) {
        String currentValue = redisTemplate.opsForValue().get(key);
        if (Objects.equals(currentValue, value)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }
}
