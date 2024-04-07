package com.github.open.courier.agent.infrastructure.discovery;

import com.github.open.courier.commons.support.ServiceDiscovery;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/5/27
 */
@RequiredArgsConstructor
public class DevProfilesServiceDiscovery implements ServiceDiscovery {

    private final DiscoveryClient discoveryClient;

    @Override
    public List<String> getServiceHostAndPort(String service) {
        return discoveryClient.getInstances(service).stream()
                .map(serviceInstance -> serviceInstance.getHost() + ":" + serviceInstance.getPort())
                .collect(Collectors.toList());
    }

}
