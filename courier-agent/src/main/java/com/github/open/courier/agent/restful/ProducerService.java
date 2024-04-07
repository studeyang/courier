package com.github.open.courier.agent.restful;

import com.github.open.courier.agent.infrastructure.config.CourierAgentProperties;
import com.github.open.courier.core.transport.*;
import com.github.open.courier.core.constant.URLConstant;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.webflux.ProxyExchange;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/5/25
 */
@Slf4j
@RestController
@Api(tags = "转发给courier producer的接口")
public class ProducerService {

    private final String cluster;
    private final String env;
    private final String proxyUrl;
    private final String proxyProducerPrefix;

    public ProducerService(CourierAgentProperties properties) {
        this.cluster = properties.getCluster();
        this.env = properties.getEnv();
        this.proxyUrl = properties.getProxyUrl();
        this.proxyProducerPrefix = properties.getProxyProducerPrefix();
    }

    @ApiOperation("消息订阅（添加集群&环境标识）")
    @PostMapping(URLConstant.PRODUCER_SUBSCRIBE)
    public Mono<ResponseEntity<byte[]>> subscribe(@RequestHeader HttpHeaders headers,
                                                  @RequestBody SubscribeRequest request, ProxyExchange<byte[]> proxy) {
        request.setCluster(cluster);
        request.setEnv(env);
        return proxy.uri(proxyUrl + proxyProducerPrefix + URLConstant.PRODUCER_SUBSCRIBE)
                .headers(HeadersFilter.doFilter(headers))
                .body(request)
                .post();
    }

    @ApiOperation("发送一条消息（消息染色）")
    @PostMapping(URLConstant.PRODUCER_SEND)
    public Mono<ResponseEntity<byte[]>> send(@RequestHeader HttpHeaders headers,
                                             @RequestBody SendMessage message, ProxyExchange<byte[]> proxy) {
        message.setCluster(cluster);
        message.setEnv(env);

        return proxy.uri(proxyUrl + proxyProducerPrefix + URLConstant.PRODUCER_SEND)
                .headers(HeadersFilter.doFilter(headers))
                .body(message)
                .post();
    }

    @ApiOperation("发送一批消息（消息染色）")
    @PostMapping(URLConstant.PRODUCER_SENDS)
    public Mono<ResponseEntity<byte[]>> send(@RequestHeader HttpHeaders headers,
                                             @RequestBody List<SendMessage> messages, ProxyExchange<byte[]> proxy) {
        messages.forEach(message -> {
            message.setCluster(cluster);
            message.setEnv(env);
        });
        return proxy.uri(proxyUrl + proxyProducerPrefix + URLConstant.PRODUCER_SENDS)
                .headers(HeadersFilter.doFilter(headers))
                .body(messages)
                .post();
    }

    @ApiOperation("客户端启动-恢复推送")
    @PostMapping(URLConstant.PRODUCER_RESUME_PUSH)
    public MessageSendResult resumePush(@RequestBody ResumePushRequest request) {
        log.info("调用了恢复推送接口: {}", request);
        return MessageSendResult.success("恢复推送接口弃用");
    }

    @ApiOperation("暂停推送")
    @PostMapping(value = URLConstant.PRODUCER_PAUSE_PUSH)
    public MessageSendResult pausePush(@RequestBody PausePushRequest request) {
        log.info("调用了暂停推送接口: {}", request);
        return MessageSendResult.success("暂停推送接口弃用");
    }

}
