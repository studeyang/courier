package com.github.open.courier.client.feign;

import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.transport.SendMessage;
import com.github.open.courier.core.transport.MessageSendResult;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * @author yanglulu
 */
@FeignClient(value = "courier-delay", url = CourierAgent.URL)
@org.springframework.cloud.openfeign.FeignClient(value = "courier-delay", url = CourierAgent.URL)
public interface DelayClient {

    @RequestMapping(value = URLConstant.DELAY_SEND, method = RequestMethod.POST)
    MessageSendResult send(@RequestBody SendMessage message);

    @RequestMapping(value = URLConstant.DELAY_SENDS, method = RequestMethod.POST)
    List<MessageSendResult> send(@RequestBody List<SendMessage> message);

}
