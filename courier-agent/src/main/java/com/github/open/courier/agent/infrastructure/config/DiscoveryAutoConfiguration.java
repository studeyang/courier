package com.github.open.courier.agent.infrastructure.config;

import com.github.open.courier.agent.infrastructure.discovery.DevProfilesServiceDiscovery;
import com.github.open.courier.agent.infrastructure.discovery.LoadBalanceServiceDiscovery;
import com.github.open.courier.commons.support.ServiceDiscovery;
import com.github.open.discovery.kubernetes.KubernetesDiscoveryClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/6/9
 */
@Configuration
public class DiscoveryAutoConfiguration {

    @Bean
    @Profile("default")
    public ServiceDiscovery devProfilesServiceDiscovery(
            @Qualifier("simpleDiscoveryClient") DiscoveryClient simpleDiscoveryClient) {
        return new DevProfilesServiceDiscovery(simpleDiscoveryClient);
    }

//    @Bean
//    @ConditionalOnMissingBean
//    public ServiceDiscovery kubernetesServiceDiscovery(KubernetesDiscoveryClient kubernetesDiscoveryClient) {
//        return new KubernetesServiceDiscovery(kubernetesDiscoveryClient);
//    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceDiscovery loadBalanceServiceDiscovery(KubernetesDiscoveryClient kubernetesDiscoveryClient) {
        return new LoadBalanceServiceDiscovery(kubernetesDiscoveryClient);
    }

}
