package com.github.open.courier.management.infrastructure.feign;

import com.github.open.courier.commons.support.CourierServer;
import com.github.open.courier.core.constant.URLConstant;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Lijiahao
 */

@FeignClient(value = "courier-consumer", url = CourierServer.CONSUMER_AGENT)
public interface ConsumerClient {

    @RequestMapping(value = URLConstant.CONSUMER_PODS, method = RequestMethod.POST)
    Map<String, List<String>> getPodsByServices(@RequestBody Set<String> request);

    @PostMapping(value = URLConstant.CONSUMER_ALARM, params = {"service", "message"})
    void alarm(@RequestParam("service") String service,
               @RequestParam("message") String message);

}
