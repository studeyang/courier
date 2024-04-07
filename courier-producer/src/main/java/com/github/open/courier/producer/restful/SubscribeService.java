package com.github.open.courier.producer.restful;

import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.producer.application.SubscribeAppService;
import com.github.open.courier.core.transport.SubscribeRequest;
import com.github.open.courier.core.transport.SubscribeResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订阅消息服务
 *
 * @author Courier
 */
@Slf4j
@Api(tags = "订阅消息服务")
@RestController
@RequiredArgsConstructor
public class SubscribeService {

    private final SubscribeAppService subscribeAppService;

    @ApiOperation("订阅消息接口")
    @ApiImplicitParam(name = "request", value = "订阅消息参数", dataType = "SubscribeRequest", required = true, paramType = "body")
    @PostMapping(URLConstant.PRODUCER_SUBSCRIBE)
    public SubscribeResult subscribe(@RequestBody SubscribeRequest request) {
        Assert.notNull(request.getCluster(), "集群名不可为空");
        Assert.notNull(request.getEnv(), "环境名不可为空");
        return subscribeAppService.subscribe(request);
    }

    @ApiOperation("服务下线")
    @ApiImplicitParam(name = "service", value = "服务下线", dataType = "string", required = true, paramType = "body")
    @PostMapping(URLConstant.PRODUCER_UNSUBSCRIBE)
    public SubscribeResult unsubscribe(@RequestBody String service) {
        return subscribeAppService.unsubscribe(service);
    }

}
