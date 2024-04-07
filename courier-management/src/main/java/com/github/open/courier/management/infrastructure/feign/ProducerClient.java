package com.github.open.courier.management.infrastructure.feign;

import com.github.open.courier.commons.support.CourierServer;
import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.transport.SendMessage;
import com.github.open.courier.core.transport.MessageSendResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * @author Courier
 */
@FeignClient(value = "courier-producer", url = CourierServer.PRODUCER_AGENT)
public interface ProducerClient {

    @RequestMapping(value = URLConstant.PRODUCER_SENDS, method = RequestMethod.POST)
    List<MessageSendResult> send(@RequestBody List<SendMessage> message);

    @RequestMapping(value = URLConstant.PRODUCER_REFRESH, method = RequestMethod.POST)
    void refreshAllListener();
}
