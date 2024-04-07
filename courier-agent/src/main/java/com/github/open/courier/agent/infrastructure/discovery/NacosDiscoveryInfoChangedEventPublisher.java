package com.github.open.courier.agent.infrastructure.discovery;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.event.NacosDiscoveryInfoChangedEvent;
import com.github.open.courier.agent.infrastructure.config.CourierAgentProperties;
import com.github.open.courier.commons.support.ServiceRegistration;
import com.github.open.discovery.kubernetes.ServicesUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

import java.util.Map;

@RequiredArgsConstructor
public class NacosDiscoveryInfoChangedEventPublisher implements ApplicationListener<ServicesUpdateEvent> {

    private final ApplicationContext applicationContext;
    private final NacosDiscoveryProperties nacosDiscoveryProperties;
    @Autowired
    private CourierAgentProperties courierAgentProperties;

    @Override
    public void onApplicationEvent(ServicesUpdateEvent event) {
        if (courierAgentProperties.isRegisterServiceScope()) {
            Map<String, String> metadata = nacosDiscoveryProperties.getMetadata();
            String service = String.join(",", event.getSource());
            metadata.put(ServiceRegistration.AGENT_DISCOVER_SCOPE, service);
            applicationContext.publishEvent(new NacosDiscoveryInfoChangedEvent(nacosDiscoveryProperties));
        }
    }

}
