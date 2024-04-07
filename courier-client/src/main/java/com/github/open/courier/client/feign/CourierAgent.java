package com.github.open.courier.client.feign;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/3/24
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CourierAgent {

    public static final String URL = "${courier.agent.url:${icec.api.agent}/courier-agent}";

}
