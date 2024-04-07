package com.github.open.courier.management.infrastructure.config;

import com.github.open.courier.commons.configuration.CourierRepositoryAutoConfiguration;
import com.github.open.courier.commons.configuration.CourierServerBaseAutoConfiguration;
import com.github.open.courier.commons.loadbalance.ILoadBalance;
import com.github.open.courier.commons.loadbalance.RandomLoadBalance;
import com.github.open.courier.commons.support.AssignConsume;
import com.github.open.courier.commons.support.CourierDiscoveryClient;
import com.github.open.courier.repository.mapper.SubscribeManageMapper;
import com.github.open.courier.management.application.service.ShardingTableAppService;
import com.github.open.courier.management.application.support.ShardingTableCheckSentinel;
import com.github.open.discovery.kubernetes.KubernetesDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 所需的bean配置
 *
 * @author Courier
 */
@Configuration
@Import({
        CourierServerBaseAutoConfiguration.class,
        CourierRepositoryAutoConfiguration.class
})
public class CourierManagementAutoConfiguration {

    @Bean
    public ShardingTableCheckSentinel shardingTableCheckSentinel(ShardingTableAppService shardingTableAppService) {
        return new ShardingTableCheckSentinel(shardingTableAppService);
    }

    @Bean
    public RandomLoadBalance randomLoadBalance() {
        return new RandomLoadBalance();
    }

    @Bean
    public AssignConsume assignConsume(SubscribeManageMapper subscribeManageMapper,
                                       ILoadBalance loadBalance) {
        return new AssignConsume(subscribeManageMapper, loadBalance);
    }

    @Bean
    public CourierDiscoveryClient courierDiscoveryClient(KubernetesDiscoveryClient discoveryClient,
                                                         AssignConsume assignConsume) {
        return new CourierDiscoveryClient(discoveryClient, null, assignConsume);
    }

}
