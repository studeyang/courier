package com.github.open.courier.core.support;

import lombok.AccessLevel;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

/**
 * kafka brokers
 */
public final class Brokers {

    @Setter(AccessLevel.PRIVATE)
    private static String value;

    @Value("${courier.brokers}")
    private void initBrokers(String brokers) {
        setValue(brokers);
    }

    /**
     * 获取brokers
     */
    public static String get() {
        return value;
    }
}
