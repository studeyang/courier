package com.github.open.courier.agent.infrastructure.feign;

import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.transport.ConsumeMessage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "courier-consumer", url = "${courier.agent.proxyUrl}${courier.agent.proxyConsumerPrefix:/courier-consumer}")
public interface ConsumerClient {

    @PostMapping(value = URLConstant.CONSUMER_RECORD)
    void record(@RequestBody List<ConsumeMessage> messages);

    @PostMapping(value = URLConstant.CONSUMER_INSERT)
    void insertBatch(@RequestBody List<ConsumeMessage> messages);

}