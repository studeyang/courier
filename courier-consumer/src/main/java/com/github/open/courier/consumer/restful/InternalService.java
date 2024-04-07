package com.github.open.courier.consumer.restful;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.github.open.courier.commons.redis.RedisClient;
import com.github.open.courier.consumer.service.biz.PushBizService;
import com.github.open.courier.repository.mapper.SubscribeGroupIdMapper;
import com.github.open.courier.core.constant.MessageConstant;
import com.github.open.courier.core.transport.PushOperationRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yanglulu
 */
@Api(tags = "内部服务")
@Slf4j
@RequestMapping("/internal")
@RestController
public class InternalService {

    private static final String PAUSED_POD = "PAUSED";
    private static final String STOPPED = "STOPPED";
    private static final String STARTED = "STARTED";

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private PushBizService pushBizService;

    @Autowired
    private SubscribeGroupIdMapper subscribeGroupIdMapper;
    @NacosInjected
    private NamingService namingService;

    @ApiOperation("操作推送")
    @ApiImplicitParam(name = "request", value = "操作推送请求", dataType = "PushOperationRequest", paramType = "body")
    @PostMapping("/operation")
    public void pushOperation(@RequestBody PushOperationRequest request) {

        Assert.notNull(request.getOperation(), () -> "操作不可为 null");

        switch (request.getOperation()) {

            case PAUSE:
                pushBizService.pausePush(request);
                break;

            case RESUME:
                pushBizService.resumePush(request);
                break;

            default:
                throw new IllegalArgumentException("不支持的操作：" + request.getOperation());
        }
    }

    @ApiOperation("清空groupid")
    @ApiImplicitParam(name = "ids", value = "id集合", dataType = "int", paramType = "body", example = "[0, 1]")
    @DeleteMapping("/groupid")
    public void deleteByIds(@RequestBody List<Integer> ids) {
        subscribeGroupIdMapper.deleteByIds(ids);
    }

    @ApiOperation("添加groupid")
    @ApiImplicitParam(name = "ids", value = "id集合", dataType = "int", paramType = "body", example = "[0, 1]")
    @PostMapping("/groupid")
    public void addByIds(@RequestBody List<Integer> ids) {
        for (int id : ids) {
            subscribeGroupIdMapper.insert(id, 0, null, null);
        }
    }

    @ApiOperation("获取节点")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "service", value = "服务名", dataType = "string", paramType = "param"),
            @ApiImplicitParam(name = "state", value = "状态（ALL, PAUSED）", dataType = "string", paramType = "param", defaultValue = "ALL")
    })
    @GetMapping("/pods")
    public List<String> getByService(@RequestParam String service, @RequestParam String state) {

        switch (state) {

            case PAUSED_POD:
                return redisClient.listAll(String.format(MessageConstant.PAUSE_LIST, service)).stream()
                        .sorted().collect(Collectors.toList());

            case STOPPED:
                return redisClient.listAll(String.format(MessageConstant.STOPPED_LIST, service)).stream()
                        .sorted().collect(Collectors.toList());

            case STARTED:
                return redisClient.listAll(String.format(MessageConstant.STARTED_LIST, service)).stream()
                        .sorted().collect(Collectors.toList());

            default:
                throw new IllegalArgumentException("无此状态 [" + state + "] 节点");
        }
    }

    @SneakyThrows
    @GetMapping("/courier-agent")
    public List<Instance> getCourierAgentInstances() {
        return this.namingService.getAllInstances("courier-agent");
    }

}
