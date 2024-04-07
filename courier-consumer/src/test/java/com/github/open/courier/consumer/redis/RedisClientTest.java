package com.github.open.courier.consumer.redis;

import com.github.open.courier.commons.redis.RedisClient;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisClientTest {

    @Autowired
    private RedisClient redisClient;

    @Test
    @Ignore
    public void add() {
        redisClient.add("courier:consumer:host:pause:courier-example1", "10.3");
        redisClient.add("courier:consumer:host:pause:courier-example1", "10.2");
    }

    @Test
    @Ignore
    public void list() {
        System.out.println(redisClient.listAll("courier:consumer:host:pause:courier-example1"));
    }

    @Test
    @Ignore
    public void remove() {
        redisClient.remove("courier:consumer:host:pause:courier-example1", "10.1");
    }

}
