package com.github.open.courier.agent.infrastructure.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.github.open.courier.agent.infrastructure.discovery.NacosDiscoveryInfoChangedEventPublisher;
import com.github.open.courier.agent.infrastructure.feign.ConsumerClient;
import com.github.open.courier.agent.infrastructure.discovery.RegistrationMetadataCustomizer;
import com.github.open.courier.agent.service.biz.BroadcastBizService;
import com.github.open.courier.agent.service.support.RestTemplatePusher;
import com.github.open.courier.commons.configuration.CourierServerBaseAutoConfiguration;
import com.github.open.discovery.kubernetes.KubernetesDiscoveryClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/5/27
 */
@Configuration
@EnableConfigurationProperties(CourierAgentProperties.class)
@Import(CourierServerBaseAutoConfiguration.class)
public class CourierAgentAutoConfiguration {

    @Bean
    @Profile("default")
    public BroadcastBizService defaultProfilesBroadcastBizService(RestTemplatePusher restTemplatePusher,
                                                                  @Qualifier("simpleDiscoveryClient") DiscoveryClient simpleDiscoveryClient,
                                                                  ConsumerClient consumerClient) {
        return new BroadcastBizService(restTemplatePusher, simpleDiscoveryClient, consumerClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public BroadcastBizService broadcastBizService(RestTemplatePusher restTemplatePusher,
                                                   KubernetesDiscoveryClient kubernetesDiscoveryClient,
                                                   ConsumerClient consumerClient) {
        return new BroadcastBizService(restTemplatePusher, kubernetesDiscoveryClient, consumerClient);
    }

    @Bean
    public RegistrationMetadataCustomizer registrationMetadataCustomizer() {
        return new RegistrationMetadataCustomizer();
    }

    @Bean
    public NacosDiscoveryInfoChangedEventPublisher discoverInfoChangePublisher(ApplicationContext applicationContext,
                                                                               NacosDiscoveryProperties nacosDiscoveryProperties) {
        return new NacosDiscoveryInfoChangedEventPublisher(applicationContext, nacosDiscoveryProperties);
    }

}
