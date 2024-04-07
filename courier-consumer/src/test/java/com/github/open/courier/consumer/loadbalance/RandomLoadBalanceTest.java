package com.github.open.courier.consumer.loadbalance;

import com.github.open.courier.commons.loadbalance.RandomLoadBalance;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class RandomLoadBalanceTest {

    private RandomLoadBalance randomLoadBalance;

    @Before
    public void before() {
        randomLoadBalance = new RandomLoadBalance();
    }

    @Test
    public void choose() {
        List<String> hosts = Arrays.asList("http://10.0.0.1", "http://10.0.0.2", "http://10.0.0.3");
        for (int i = 0; i < 10; i++) {
            System.out.println(randomLoadBalance.choose(hosts));
        }
    }

}