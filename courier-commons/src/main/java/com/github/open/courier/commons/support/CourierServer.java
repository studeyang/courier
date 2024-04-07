package com.github.open.courier.commons.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/3/24
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CourierServer {

    public static final String PRODUCER_AGENT = "${courier.server.producer:service-courier-producer:11111}";
    public static final String CONSUMER_AGENT = "${courier.server.consumer:service-courier-consumer:11112}";
    public static final String MANAGEMENT_AGENT = "${courier.server.management:service-courier-management:11113}";

}
