package com.github.open.courier.consumer.service.support;

import com.github.open.courier.core.transport.ConsumeMessage;
import com.github.open.courier.core.converter.ExceptionConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * RestTemplate推送器
 *
 * @author Courier
 */
@Slf4j
@Component
public class RestTemplatePusher {

    @Autowired
    @Qualifier("courierServerRestTemplate")
    private RestTemplate restTemplate;
    @Autowired
    private ConsumeRecorder consumeRecorder;

    public void pushByServiceOrUrl(String url, String service, List<ConsumeMessage> messages) {

        PushContext context = new PushContext().setService(service).setUrl(url).setMessages(messages);

        push(context);
    }

    /**
     * 推送一批消息
     *
     * @param context 推送上下文
     */
    public void push(PushContext context) {

        List<ConsumeMessage> messages = context.getMessages();

        if (CollectionUtils.isEmpty(messages)) {
            return;
        }

        List<String> consumerIds = new ArrayList<>(messages.size());
        List<String> messageIds = new ArrayList<>(messages.size());
        for (ConsumeMessage message : messages) {
            consumerIds.add(message.getId());
            messageIds.add(message.getMessageId());
        }

        Date beforePushTime = new Date();

        String cause = null;
        long start = System.currentTimeMillis();
        try {
            restTemplate.postForEntity(context.getUrl(), messages, Void.class);
        } catch (RestClientResponseException e) {
            log.error(e.getMessage(), e);
            // e.getResponseBodyAsString() 似乎有编码问题
            cause = new String(e.getResponseBodyAsByteArray());

        } catch (ResourceAccessException e) {
            // 节点连接超时，有可能已经重启了
            log.warn("消息推送超时， service: {}, consumerIds:{}, messageIds:{}, url: {}, cost: {}",
                    context.getService(), consumerIds, messageIds, context.getUrl(), System.currentTimeMillis() - start);
            if (context.canRepush()) {
                context.retriesPlus();
                log.warn("重试中[{}]，consumerIds:{}, messageIds:{}, url: {}", context.getRetries(), consumerIds, messageIds, context.getUrl());
                push(context);
            } else {
                cause = ExceptionConverter.getCause(e);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            cause = ExceptionConverter.getCause(e);
        }

        try {
            if (cause == null) {
                consumeRecorder.consumeSuccess(context, beforePushTime);
            } else {
                log.error("RestTemplate发送消息失败, service: {}, consumerIds:{}, messageIds:{}, url:{}, cause:{}",
                        context.getService(), consumerIds, messageIds, context.getUrl(), cause);
                consumeRecorder.consumeFails(context, cause);
            }
        } catch (Exception e) {
            log.error("消息存档异常", e);
        }
    }

}
