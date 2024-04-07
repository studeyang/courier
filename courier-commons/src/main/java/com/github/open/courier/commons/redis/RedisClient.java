package com.github.open.courier.commons.redis;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author yanglulu
 */
@RequiredArgsConstructor
public class RedisClient {

    private final RedisTemplate<String, String> redisTemplate;

    public List<String> listAll(String key) {
        return new ArrayList<>(redisTemplate.opsForSet().members(key));
    }

    public void add(String key, String value) {
        redisTemplate.opsForSet().add(key, value);
    }

    public void add(String key, String value, int expireMinutes) {
        SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        setOperations.add(key, value);
        setOperations.getOperations().expire(key, expireMinutes, TimeUnit.MINUTES);
    }

    public void addAll(String key, List<String> collection) {
        for (String value : collection) {
            add(key, value);
        }
    }

    public void remove(String key, Object value) {
        if (StringUtils.isEmpty(key)) {
            return;
        }
        redisTemplate.opsForSet().remove(key, value);
    }

    public void removeAll(String key) {
        listAll(key).forEach(value -> remove(key, value));
    }

}
