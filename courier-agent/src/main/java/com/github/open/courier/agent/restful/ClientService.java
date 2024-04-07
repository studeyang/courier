package com.github.open.courier.agent.restful;

import com.github.open.courier.agent.service.biz.ReceiveBizService;
import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.transport.ConsumeMessage;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/5/27
 */
@Api(tags = "转发给courier client的接口")
@RestController
public class ClientService {

    @Autowired
    private ReceiveBizService receiveBizService;

    /**
     * 接收一批consumeMessage并消费
     */
    @PostMapping(URLConstant.CLIENT_RECEIVES)
    public void receive(@RequestBody List<ConsumeMessage> consumeMessages) {
        receiveBizService.receive(consumeMessages);
    }

}
