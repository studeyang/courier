package com.github.open.courier.admin.client;

import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.transport.SubscribeManageRequest;
import com.github.open.courier.core.transport.SubscribeResult;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author Lijiahao
 */
@FeignClient(value = "courier-producer", url = "${courier.server.producer:service-courier-producer:11111}")
public interface ProducerClient {

    @PostMapping(URLConstant.PRODUCER_UNSUBSCRIBE)
    SubscribeResult unsubscribeService(SubscribeManageRequest request);

}
