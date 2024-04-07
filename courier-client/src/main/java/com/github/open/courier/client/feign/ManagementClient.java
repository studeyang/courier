package com.github.open.courier.client.feign;

import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.transport.ConsumeFailMessage;
import com.github.open.courier.core.transport.MessageConsumeTime;
import com.github.open.courier.core.transport.SendFailMessage;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(value = "courier-management", url = CourierAgent.URL)
@org.springframework.cloud.openfeign.FeignClient(value = "courier-management", url = CourierAgent.URL)
public interface ManagementClient {

    @RequestMapping(value = URLConstant.MANAGEMENT_SEND_FAIL, method = RequestMethod.POST)
    void sendFail(@RequestBody SendFailMessage message);

    @RequestMapping(value = URLConstant.MANAGEMENT_SEND_FAILS, method = RequestMethod.POST)
    void sendFail(@RequestBody List<SendFailMessage> messages);

    @RequestMapping(value = URLConstant.MANAGEMENT_HANDLE_SUCCESS, method = RequestMethod.POST)
    void handleSuccess(@RequestBody List<MessageConsumeTime> messages);

    @RequestMapping(value = URLConstant.MANAGEMENT_HANDLE_FAIL, method = RequestMethod.POST)
    void handleFail(@RequestBody List<ConsumeFailMessage> messages);
}
