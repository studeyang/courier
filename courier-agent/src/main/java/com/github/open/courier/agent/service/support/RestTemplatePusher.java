package com.github.open.courier.agent.service.support;

import com.github.open.courier.agent.infrastructure.config.CourierAgentProperties;
import com.github.open.courier.commons.loadbalance.LoadBalancer;
import com.github.open.courier.commons.support.ServiceDiscovery;
import com.github.open.courier.core.transport.ConsumeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.open.courier.core.constant.URLConstant.CLIENT_RECEIVES;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/5/27
 */
@Slf4j
@Component
public class RestTemplatePusher {

    private final RestTemplate restTemplate;
    private final ServiceDiscovery serviceDiscovery;
    private final Map<String, String> discovery;

    public RestTemplatePusher(@Qualifier("courierServerRestTemplate") RestTemplate restTemplate,
                              ServiceDiscovery serviceDiscovery,
                              CourierAgentProperties properties) {
        this.restTemplate = restTemplate;
        this.serviceDiscovery = serviceDiscovery;
        this.discovery = properties.getDiscovery();
    }

    public void pushByService(String service, List<ConsumeMessage> messages) {

        if (CollectionUtils.isEmpty(messages)) {
            return;
        }

        List<String> hosts = serviceDiscovery.getServiceHostAndPort(service);

        messages.stream()
                .collect(Collectors.groupingBy(
                        message -> message.getPrimaryKey() == null ? "" : message.getPrimaryKey()))
                .forEach((primaryKey, msgs) -> {
                    String url = selectUrl(primaryKey, service, hosts);
                    doPush(service, url, msgs);
                });
    }

    public void pushByUrl(String service, String url, List<ConsumeMessage> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }
        doPush(service, url, messages);
    }

    private String selectUrl(String primaryKey, String service, List<String> hosts) {
        String host;
        if (primaryKey.isEmpty()) {
            host = LoadBalancer.selectRandomHost(hosts);
        } else {
            host = LoadBalancer.selectHashRoundHost(primaryKey, hosts);
        }
        return discovery.getOrDefault(service, "http://" + host + CLIENT_RECEIVES);
    }

    private void doPush(String service, String url, List<ConsumeMessage> messages) {
        long start = System.currentTimeMillis();
        restTemplate.postForEntity(url, messages, Void.class);
        long cost = System.currentTimeMillis() - start;
        log.info("toService={}, url={}, size={}, length={}, cost={}ms", service, url, messages.size(),
                messages.toString().length(), cost);
    }

}
