package com.github.open.courier.management.application.jobhandler;

import com.github.open.courier.core.converter.ExceptionConverter;
import com.github.open.courier.core.transport.ConsumeMessage;
import com.xxl.job.core.handler.IJobHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 抽象的重推器
 */
@Slf4j
abstract class AbstractRepusher extends IJobHandler {

    @Autowired
    private RestTemplate restTemplate;

    public void push(String url, List<ConsumeMessage> messages) {

        if (CollectionUtils.isEmpty(messages)) {
            return;
        }

        String cause = null;
        try {
            restTemplate.postForEntity(url, messages, Void.class);
        } catch (RestClientResponseException e) {
            cause = new String(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            cause = ExceptionConverter.getCause(e);
        }

        List<String> ids = messages.stream().map(ConsumeMessage::getId).collect(Collectors.toList());
        if (cause == null) {
            log.info("推送成功, url:{}, cid:{}", url, ids);
        } else {
            log.error("推送失败!!! url:{}, cid:{}, cause:{}", url, ids, cause);
        }
    }
}
