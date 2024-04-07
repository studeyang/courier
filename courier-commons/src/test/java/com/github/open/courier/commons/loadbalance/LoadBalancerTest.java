package com.github.open.courier.commons.loadbalance;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 1.0 2022/8/29
 */
public class LoadBalancerTest {

    @Test
    public void selectHashRoundHost() {

        List<String> hosts = new ArrayList<>();
        hosts.add("10.0.0.1");
        hosts.add("10.0.0.2");
        hosts.add("10.0.0.3");

        System.out.println(LoadBalancer.selectHashRoundHost("0825", hosts));
        System.out.println(LoadBalancer.selectHashRoundHost("0826", hosts));
        System.out.println(LoadBalancer.selectHashRoundHost("0827", hosts));
        System.out.println(LoadBalancer.selectHashRoundHost("0828", hosts));
        System.out.println(LoadBalancer.selectHashRoundHost("a", hosts));
        System.out.println(LoadBalancer.selectHashRoundHost("b", hosts));

        int index = "20220824".hashCode() % 3;
        System.out.println(Math.abs(index));
    }

}