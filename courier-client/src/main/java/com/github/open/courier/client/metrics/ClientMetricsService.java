package com.github.open.courier.client.metrics;

import com.github.open.courier.client.consumer.internal.ClientListenerContainer;
import com.github.open.courier.core.constant.URLConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Client指标服务
 *
 * @author Courier
 */
@RestController
@RequiredArgsConstructor
public class ClientMetricsService {

    final ClientListenerContainer listenerContainer;

    @GetMapping(URLConstant.CLIENT_METRICS)
    public ClientMetrics metrics() {
        return new ClientMetrics();
    }

}