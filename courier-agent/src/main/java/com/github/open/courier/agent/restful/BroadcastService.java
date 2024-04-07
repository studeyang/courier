package com.github.open.courier.agent.restful;

import com.github.open.courier.agent.service.biz.BroadcastBizService;
import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.transport.ConsumeMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/5/27
 */
@RestController
@AllArgsConstructor
@Api(tags = "广播消息相关接口")
public class BroadcastService {

    @Autowired
    private BroadcastBizService broadcastBizService;

    @ApiOperation("PULL消费模式的广播消费")
    @PostMapping(URLConstant.CONSUMER_BROADCAST)
    public void consumeBroadcast(@RequestBody List<ConsumeMessage> messages) {
        broadcastBizService.handle(messages);
    }

}
