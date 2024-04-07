package com.github.open.courier.agent.restful;

import com.github.open.courier.agent.infrastructure.config.CourierAgentProperties;
import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.transport.SendMessage;
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
 * @since 2.0 2022/6/18
 */
@Slf4j
@RestController
@Api(tags = "转发给courier delay的接口")
public class DelayService {

    private final String cluster;
    private final String env;
    private final String proxyUrl;
    private final String proxyDelayPrefix;

    public DelayService(CourierAgentProperties properties) {
        this.cluster = properties.getCluster();
        this.env = properties.getEnv();
        this.proxyUrl = properties.getProxyUrl();
        this.proxyDelayPrefix = properties.getProxyDelayPrefix();
    }

    @ApiOperation("发送一条消息（消息染色）")
    @PostMapping(URLConstant.DELAY_SEND)
    public Mono<ResponseEntity<byte[]>> send(@RequestHeader HttpHeaders headers,
                                             @RequestBody SendMessage message, ProxyExchange<byte[]> proxy) {
        message.setCluster(cluster);
        message.setEnv(env);
        return proxy.uri(proxyUrl + proxyDelayPrefix + URLConstant.DELAY_SEND)
                .headers(HeadersFilter.doFilter(headers))
                .body(message)
                .post();
    }

    @ApiOperation("发送一批消息（消息染色）")
    @PostMapping(URLConstant.DELAY_SENDS)
    public Mono<ResponseEntity<byte[]>> send(@RequestHeader HttpHeaders headers,
                                             @RequestBody List<SendMessage> messages, ProxyExchange<byte[]> proxy) {
        messages.forEach(message -> {
            message.setCluster(cluster);
            message.setEnv(env);
        });
        return proxy.uri(proxyUrl + proxyDelayPrefix + URLConstant.DELAY_SENDS)
                .headers(HeadersFilter.doFilter(headers))
                .body(messages)
                .post();
    }

}
