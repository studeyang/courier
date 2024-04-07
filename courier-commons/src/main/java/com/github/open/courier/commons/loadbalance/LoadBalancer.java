package com.github.open.courier.commons.loadbalance;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoadBalancer {

    private static RandomLoadBalance randomLoadBalance = new RandomLoadBalance();

    public static String selectRandomHost(List<String> hosts) {
        return randomLoadBalance.choose(hosts);
    }

    public static String selectHashRoundHost(String key, List<String> hosts) {
        int index = key.hashCode() % hosts.size();
        return hosts.get(Math.abs(index));
    }

}
