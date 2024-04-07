package com.github.open.courier.agent.infrastructure.discovery;

import com.github.open.courier.commons.support.ServiceDiscovery;
import com.github.open.discovery.kubernetes.KubernetesDiscoveryClient;
import com.github.open.discovery.kubernetes.KubernetesServiceInstance;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/5/27
 */
@RequiredArgsConstructor
public class KubernetesServiceDiscovery implements ServiceDiscovery {

    private final KubernetesDiscoveryClient discoveryClient;

    @Override
    public List<String> getServiceHostAndPort(String service) {
        List<String> hosts = new ArrayList<>();
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(service);
        for (ServiceInstance serviceInstance : serviceInstances) {
            KubernetesServiceInstance ks = (KubernetesServiceInstance) serviceInstance;
            hosts.add("service-" + ks.getServiceId() + "." + ks.getNamespace() + ":" + ks.getPort());
        }
        Assert.notEmpty(hosts, "未发现服务：" + service);
        return hosts;
    }

}
