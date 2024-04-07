package com.github.open.courier.core.utils;

import org.junit.Test;

import java.security.SecureRandom;

public class IpUtilsTest {

    @Test
    public void randomIp() {
        System.out.println(new SecureRandom().nextInt(1));
        System.out.println(IpUtils.randomIp());
    }
}