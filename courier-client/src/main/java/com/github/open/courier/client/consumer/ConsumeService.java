package com.github.open.courier.client.consumer;

import com.github.open.courier.client.consumer.internal.ConsumeSupport;
import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.transport.ConsumeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 消费服务
 *
 * @author Courier
 */
@RestController
@RequiredArgsConstructor
public class ConsumeService {

    private final ConsumeSupport consumeSupport;

    /**
     * 接收一个consumeMessage并消费
     */
    @PostMapping(URLConstant.CLIENT_RECEIVE)
    public void receive(@RequestBody ConsumeMessage consumeMessage) {
        consumeSupport.consume(consumeMessage);
    }

    /**
     * 接收一批consumeMessage并消费
     */
    @PostMapping(URLConstant.CLIENT_RECEIVES)
    public void receive(@RequestBody List<ConsumeMessage> consumeMessages) {
        consumeSupport.consume(consumeMessages);
    }
}
