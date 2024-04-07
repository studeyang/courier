package com.github.open.courier.client.consumer.internal;

import com.github.open.courier.client.feign.ProducerClient;
import com.github.open.courier.core.support.CourierContext;
import com.github.open.courier.core.constant.ClientState;
import com.github.open.courier.core.exception.SubscribeException;
import com.github.open.courier.core.support.AutoStartupLifecycle;
import com.github.open.courier.core.support.Retryable;
import com.github.open.courier.core.transport.PausePushRequest;
import com.github.open.courier.core.transport.ResumePushRequest;
import com.github.open.courier.core.transport.SubscribeRequest;
import com.github.open.courier.core.transport.SubscribeResult;
import com.github.rholder.retry.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import static com.github.open.courier.core.constant.MessageConstant.SUBSCRIBE_PHASE;

/**
 * 订阅初始化
 *
 * @author Courier
 */
@Slf4j
@RequiredArgsConstructor
public class SubscribeInitializer extends AutoStartupLifecycle implements Retryable<SubscribeResult> {

    private final MessageHandlerContainer handlerContainer;
    private final ProducerClient producerClient;

    @Value("${server.port}")
    private Integer port;

    @Override
    public void onStart() {

        SubscribeRequest request = handlerContainer.getSubscribeRequest();

        // 打印订阅的消息
        if (CollectionUtils.isEmpty(request.getTopicGroups())) {
            log.info("本服务没有订阅kafka消息");
        } else {
            request.getTopicGroups().forEach(tg -> log.info("kafka订阅消息, {}", tg));
        }

        /*
         * 1.订阅接口调用成功(包括重试), 返回订阅成功, 正常结束
         * 2.订阅接口调用成功(包括重试), 返回订阅失败, 抛异常
         * 3.订阅接口调用失败(重试后依然失败), 抛异常
         */
        SubscribeResult result;
        try {
            result = build().call(() -> producerClient.subscribe(request));
        } catch (Exception e) {
            // 3 订阅接口一直调不通, 抛异常
            throw new SubscribeException("kafka重试订阅消息失败", e);
        }

        if (result != null && result.isSuccess()) {
            // 1 订阅成功, 打印info日志
            log.info(result.getReason());
        } else {
            // 2 订阅失败, 肯定是客户端的消费代码写的有问题, 抛异常
            throw new SubscribeException(result == null ? "kafka订阅消息失败" : result.getReason());
        }

        // 服务可用，通知 courier-consumer 可推送消息至该节点
        ResumePushRequest resumePushRequest = new ResumePushRequest()
                .setService(CourierContext.getService())
                .setIp(getHostAddress())
                .setPort(port)
                .setClientState(ClientState.STARTING);
        producerClient.resumePush(resumePushRequest);
    }

    /**
     * 如果订阅接口抛异常, 则重试, 顺便打个日志
     */
    @Override
    public <V> void onRetry(Attempt<V> attempt) {
        if (attempt.hasException()) {
            log.warn("kafka订阅消息失败, 正在重试: {}", attempt.getExceptionCause().getMessage());
        }
    }

    /**
     * 最多重试订阅5次
     */
    @Override
    public StopStrategy getStopStrategy() {
        return StopStrategies.stopAfterAttempt(3);
    }

    /**
     * 重试间隔, 1s 2s 4s 8s 16s
     */
    @Override
    public WaitStrategy getWaitStrategy() {
        return WaitStrategies.exponentialWait(500, 1, TimeUnit.MINUTES);
    }

    @Override
    public int getPhase() {
        return SUBSCRIBE_PHASE;
    }

    @Override
    public void onStop() {
        // 服务停止，通知 courier-consumer 停止推送消息至该节点
        PausePushRequest pausePushRequest = new PausePushRequest()
                .setService(CourierContext.getService())
                .setIp(getHostAddress())
                .setPort(port)
                .setClientState(ClientState.STOPPING);
        producerClient.pausePush(pausePushRequest);
    }

    private String getHostAddress() {

        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("获取服务 ip 失败", e);
            return "";
        }
    }
}
