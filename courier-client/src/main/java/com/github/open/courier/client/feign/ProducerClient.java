package com.github.open.courier.client.feign;

import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.transport.*;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * 注意：这里每个方法上的 @RequestMapping 不能改为 @PostMapping.
 * <p>如果改了，业务进程引用后启动报错：</p>
 * <p>java.lang.IllegalStateException: Method record not annotated with HTTP method type (ex. GET, POST)</p>
 *
 * @author Courier
 */
@FeignClient(value = "courier-producer", url = CourierAgent.URL)
@org.springframework.cloud.openfeign.FeignClient(value = "courier-producer", url = CourierAgent.URL)
public interface ProducerClient {

    @RequestMapping(value = URLConstant.PRODUCER_SEND, method = RequestMethod.POST)
    MessageSendResult send(@RequestBody SendMessage message);

    @RequestMapping(value = URLConstant.PRODUCER_SENDS, method = RequestMethod.POST)
    List<MessageSendResult> send(@RequestBody List<SendMessage> message);

    @RequestMapping(value = URLConstant.PRODUCER_SUBSCRIBE, method = RequestMethod.POST)
    SubscribeResult subscribe(@RequestBody SubscribeRequest request);

    @RequestMapping(value = URLConstant.PRODUCER_PAUSE_PUSH, method = RequestMethod.POST)
    MessageSendResult pausePush(@RequestBody PausePushRequest request);

    @RequestMapping(value = URLConstant.PRODUCER_RESUME_PUSH, method = RequestMethod.POST)
    MessageSendResult resumePush(@RequestBody ResumePushRequest request);

}
