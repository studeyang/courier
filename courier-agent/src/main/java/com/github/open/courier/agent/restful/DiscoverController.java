package com.github.open.courier.agent.restful;

import com.github.open.courier.agent.infrastructure.config.CourierAgentProperties;
import com.github.open.discovery.kubernetes.KubernetesDiscoveryClient;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yanglulu
 * @date 2022/3/11
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "服务发现")
@RequestMapping("discover")
public class DiscoverController {

    private final KubernetesDiscoveryClient discoveryClient;
    private final CourierAgentProperties courierAgentProperties;

    @ApiOperation("获取 pod 节点 ip")
    @GetMapping
    public List<String> getPod(@RequestParam(value = "service") String service) {
        List<ServiceInstance> instances = discoveryClient.getInstances(service);
        return instances.stream()
                .map(ServiceInstance::getHost)
                .collect(Collectors.toList());
    }

    @ApiOperation("批量获取 pod 节点 ip")
    @PostMapping
    public Map<String, List<String>> getPod(@RequestBody List<String> services) {

        Map<String, List<String>> result = Maps.newHashMap();

        for (String service : services) {
            List<String> pods = getPod(service);
            result.put(service, pods);
        }

        return result;
    }

    @GetMapping("/services")
    public List<String> getServices() {
        return discoveryClient.getServices();
    }

    @GetMapping("/discoveryMap")
    public Map<String, String> getDiscoveryMap() {
        return courierAgentProperties.getDiscovery();
    }

    @GetMapping("/ping")
    public void ping() {

    }

}
