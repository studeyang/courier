package com.github.open.courier.commons.redis;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Courier
 */
@RequiredArgsConstructor
public class RedisHelper {

    @Getter
    private final RedisTemplate<String, Object> redisTemplate;


    public void set(String key, Object value) {
        if (StringUtils.isBlank(key) ||  Objects.isNull(value)) {
            return;
        }
        redisTemplate.opsForValue().set(key, value);
    }

    public Object get(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return redisTemplate.opsForValue().get(key);
    }

    public boolean expire(String key, long timeout, TimeUnit unit) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        return redisTemplate.expire(key, timeout, unit);
    }

    public boolean setIfAbsent(String key, Object value) {
        if (StringUtils.isBlank(key) || Objects.isNull(value)) {
            return false;
        }
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    public Long add(String key, Object value) {

        if (StringUtils.isBlank(key) || Objects.isNull(value)) {
            return null;
        }

        return redisTemplate.opsForSet().add(key, value);
    }

    public Set<Object> members(String key) {

        if (StringUtils.isBlank(key)) {
            return null;
        }

        return redisTemplate.opsForSet().members(key);
    }


    public void leftPush(String key, String value) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
            return;
        }
        redisTemplate.opsForList().leftPush(key, value);
    }

    public Object rightPop(String key, long time, TimeUnit timeUnit) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        return redisTemplate.opsForList().rightPop(key, time, timeUnit);
    }

    public Boolean zAdd(String key, String messageId, double score) {
        return redisTemplate.opsForZSet().add(key, messageId, score);
    }

    public Long zListAdd(String key, List<String> messageIds, double score) {
        Set<ZSetOperations.TypedTuple<Object>> tuples = new HashSet<>();
        messageIds.forEach(messageId -> {
            ZSetOperations.TypedTuple<Object> tuple = new DefaultTypedTuple<>(messageId, score);
            tuples.add(tuple);
        });
        return redisTemplate.opsForZSet().add(key, tuples);
    }

    public Set<Object> zRangeByScore(String key, double min, double max) {
        Set<Object> set = redisTemplate.opsForZSet().rangeByScore(key, min, max);
        if (Objects.isNull(set)) {
            return Sets.newHashSet();
        }
        return set;
    }

    public Long zRemByValueWithScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }

    public Long zRemByValue(String key, String value) {
        return redisTemplate.opsForZSet().remove(key, value);
    }
}
