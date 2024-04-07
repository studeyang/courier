package com.github.open.courier.agent.infrastructure.discovery;

import com.github.open.courier.agent.infrastructure.config.CourierAgentProperties;
import com.github.open.courier.commons.support.ServiceDiscovery;
import com.github.open.discovery.kubernetes.KubernetesDiscoveryClient;
import com.github.open.discovery.kubernetes.KubernetesServiceInstance;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/08/02
 */
@RequiredArgsConstructor
public class LoadBalanceServiceDiscovery implements ServiceDiscovery {

    private final KubernetesDiscoveryClient discoveryClient;
    @Autowired
    private CourierAgentProperties courierAgentProperties;

    @Override
    public List<String> getServiceHostAndPort(String service) {

        // 这个服务是不是别名
        boolean isAlias = courierAgentProperties.getAlias().containsKey(service);
        if (isAlias) {
            // 是别名，则用 spring.application.name 去获取 ip
            service = courierAgentProperties.getAlias().get(service);
        }

        List<String> hosts = new ArrayList<>();
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(service);
        for (ServiceInstance serviceInstance : serviceInstances) {
            KubernetesServiceInstance ks = (KubernetesServiceInstance) serviceInstance;
            hosts.add(ks.getHost() + ":" + ks.getPort());
        }
        Assert.notEmpty(hosts, "未发现服务：" + service);
        return hosts;
    }

}
