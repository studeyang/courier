package com.github.open.courier.commons.configuration;

import com.github.open.courier.commons.redis.RedisClient;
import com.github.open.courier.commons.redis.RedisHelper;
import io.github.open.toolkit.config.annotation.PrepareConfigurations;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author yanglulu
 */
@PrepareConfigurations("__common_redis_.yml")
public class CourierRedisAutoConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean
    public RedisHelper redisHelper(RedisTemplate<String, Object> redisTemplate) {
        return new RedisHelper(redisTemplate);
    }

    @Bean
    public RedisClient redisClient(RedisTemplate<String, String> redisTemplate) {
        return new RedisClient(redisTemplate);
    }

}
