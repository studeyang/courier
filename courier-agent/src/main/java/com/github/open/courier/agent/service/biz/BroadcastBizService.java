package com.github.open.courier.agent.service.biz;

import com.github.open.courier.agent.infrastructure.feign.ConsumerClient;
import com.github.open.courier.agent.service.support.RestTemplatePusher;
import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.support.id.MessageId;
import com.github.open.courier.core.transport.ConsumeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 广播消息处理器
 *
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/5/27
 */
@Slf4j
@AllArgsConstructor
public class BroadcastBizService {

    private final RestTemplatePusher restTemplatePusher;
    private final DiscoveryClient discoveryClient;
    private final ConsumerClient consumerClient;

    public void handle(List<ConsumeMessage> messages) {

        if (CollectionUtils.isEmpty(messages)) {
            return;
        }

        String service = messages.get(0).getToService();

        List<String> urls = getServiceUrlWithMessagesReceive(service);

        // 如果没有url, 说明此时service没有一个存活实例, 所以return
        if (CollectionUtils.isEmpty(urls)) {
            log.warn("广播消息无可广播的节点, service:{}, messages:{}", service, messages);
            return;
        }

        log.info("广播消息, service:{}, urls:{}", service, urls);

        // 由于1条消息要发往多个实例(比如3个节点), 而在此前1条消息已经插入Record表1条数据
        // 所以需要多插入另外2个节点的Record, 保证每个消息消费都有对应的Record
        for (int i = 0; i < urls.size(); i++) {
            if (i != 0) {
                String id = MessageId.getId();
                messages.forEach(m -> m.setId(id));
                // 这里的循环次数不会很多，理论上来说，一个 pod 消费了就记录，不受其他 pod 异常影响，合乎逻辑
                try {
                    consumerClient.insertBatch(messages);
                } catch (Exception e) {
                    log.error("消费记录入库失败, cid: {}, mid: {}", id, getMid(messages), e);
                }
            }
            try {
                restTemplatePusher.pushByUrl(service, urls.get(i), messages);
            } catch (Exception e) {
                log.error("广播消息异常, toService: {}, url: {}, mid: {}", service, urls.get(i), getMid(messages));
            }
        }
    }

    private String getMid(List<ConsumeMessage> consumeMessages) {
        return consumeMessages.stream().map(ConsumeMessage::getMessageId)
                .collect(Collectors.joining(","));
    }

    List<String> getServiceUrlWithMessagesReceive(String service) {
        List<ServiceInstance> instances = discoveryClient.getInstances(service);

        if (CollectionUtils.isEmpty(instances)) {
            return Collections.emptyList();
        }

        return instances.stream()
                .map(instance -> URLConstant.HTTP_PREFIX + instance.getHost() + ':' + instance.getPort()
                        + URLConstant.CLIENT_RECEIVES)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
    }

}
