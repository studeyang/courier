package com.github.open.courier.consumer.service.support;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.naming.NamingService;
import com.github.open.courier.commons.loadbalance.LoadBalancer;
import com.github.open.courier.commons.support.ServiceRegistration;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/7/29
 */
@Component
public class NacosDiscoverySupport {

    @NacosInjected
    private NamingService namingService;

    @SneakyThrows
    public String selectUrl(String toCluster, String env) {
        List<String> hosts = namingService.getAllInstances("courier-agent",
                        Collections.singletonList(toCluster))
                .stream()
                .filter(instance -> env.equals(instance.getMetadata().get(ServiceRegistration.AGENT_ENV)))
                .map(instance -> instance.getIp() + ":" + instance.getPort())
                .collect(Collectors.toList());
        return LoadBalancer.selectRandomHost(hosts);
    }

    @SneakyThrows
    public String selectUrl(String toCluster) {
        List<String> hosts = namingService.getAllInstances("courier-agent",
                        Collections.singletonList(toCluster))
                .stream()
                .map(instance -> instance.getIp() + ":" + instance.getPort())
                .collect(Collectors.toList());
        return LoadBalancer.selectRandomHost(hosts);
    }

}
