package com.github.open.courier.agent.infrastructure.discovery;

import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosRegistrationCustomizer;
import com.github.open.courier.agent.infrastructure.config.CourierAgentProperties;
import com.github.open.courier.commons.support.ServiceRegistration;
import com.github.open.discovery.kubernetes.KubernetesDiscoveryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RegistrationMetadataCustomizer implements NacosRegistrationCustomizer {

    @Autowired
    private KubernetesDiscoveryClient kubernetesDiscoveryClient;
    @Autowired
    private CourierAgentProperties properties;
    @Value("${spring.profiles.active:default}")
    private String profiles;

    @Override
    public void customize(NacosRegistration registration) {
        Map<String, String> metadata = registration.getMetadata();
        if (properties.isRegisterServiceScope()) {
            List<String> services = new ArrayList<>();
            services.addAll(properties.getDiscovery().keySet());
            services.addAll(getServicesWithAlias());
            String service = String.join(",", services);
            metadata.put(ServiceRegistration.AGENT_DISCOVER_SCOPE, service);
        }
        metadata.put(ServiceRegistration.AGENT_ENV, properties.getEnv());

        // TODO 临时代码
        if ("cassmall".equals(properties.getCluster()) && "beta-penglai".equals(profiles)) {
            registration.getNacosDiscoveryProperties().setIp("10.2.60.17");
        }
    }

    private List<String> getServicesWithAlias() {
        return kubernetesDiscoveryClient.getServices()
                .stream()
                .filter(service -> !properties.getExcludes().contains(service))
                .map(this::getAliasIfNecessary).collect(Collectors.toList());
    }

    private String getAliasIfNecessary(String service) {
        for (String alias : properties.getAlias().keySet()) {
            String applicationName = properties.getAlias().get(alias);
            // has alias
            if (applicationName.equals(service)) {
                return alias;
            }
        }
        return service;
    }

}
