package com.github.open.courier.delay.client;

import com.github.open.courier.commons.support.CourierServer;
import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.transport.SendFailMessage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(value = "courier-management", url = CourierServer.MANAGEMENT_AGENT)
public interface ManagementClient {

    @RequestMapping(value = URLConstant.MANAGEMENT_SEND_FAIL, method = RequestMethod.POST)
    void sendFail(@RequestBody SendFailMessage message);

    @RequestMapping(value = URLConstant.MANAGEMENT_SEND_FAILS, method = RequestMethod.POST)
    void sendFail(@RequestBody List<SendFailMessage> messages);

}
