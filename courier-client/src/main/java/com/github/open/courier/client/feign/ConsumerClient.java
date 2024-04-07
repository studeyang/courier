package com.github.open.courier.client.feign;

import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.transport.ConsumeMessage;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author Courier
 */
@FeignClient(value = "courier-consumer", url = CourierAgent.URL)
@org.springframework.cloud.openfeign.FeignClient(value = "courier-consumer", url = CourierAgent.URL)
public interface ConsumerClient {

    @RequestMapping(value = URLConstant.CONSUMER_RECORD, method = RequestMethod.POST)
    void record(@RequestBody List<ConsumeMessage> messages);

    @RequestMapping(value = URLConstant.CONSUMER_BROADCAST, method = RequestMethod.POST)
    void consumeBroadcast(@RequestBody List<ConsumeMessage> messages);

    @RequestMapping(value = URLConstant.CONSUMER_ALARM, method = RequestMethod.POST)
    void alarm(@RequestParam("service") String service, @RequestParam("message") String message);

    @RequestMapping(value = URLConstant.CONSUMER_ALARM_RECOVERY, method = RequestMethod.POST)
    void recovery(@RequestParam("service") String service, @RequestParam("message") String message);
}
