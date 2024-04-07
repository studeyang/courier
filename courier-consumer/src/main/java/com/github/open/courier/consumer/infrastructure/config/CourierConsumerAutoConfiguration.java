package com.github.open.courier.consumer.infrastructure.config;

import com.github.open.courier.commons.configuration.CourierRedisAutoConfiguration;
import com.github.open.courier.commons.configuration.CourierRepositoryAutoConfiguration;
import com.github.open.courier.commons.configuration.CourierServerBaseAutoConfiguration;
import com.github.open.courier.commons.support.CourierServerProperties;
import com.github.open.courier.consumer.service.listener.ConsumerListenerContainer;
import com.github.open.courier.consumer.service.listener.DefaultProfilesConsumerListenerContainer;
import com.github.open.courier.core.support.Brokers;
import com.github.open.courier.core.support.CourierContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import java.util.Set;

/**
 * 注入所需的bean
 *
 * @author Courier
 */
@Configuration
@Import({
        CourierServerBaseAutoConfiguration.class,
        CourierRedisAutoConfiguration.class,
        CourierRepositoryAutoConfiguration.class
})
public class CourierConsumerAutoConfiguration {

    @Bean
    public Brokers brokers() {
        return new Brokers();
    }

    /**
     * 下面这两个 bean 要注意装配的顺序
     */

    @Bean
    @Profile("default")
    @ConditionalOnBean(CourierContext.class)
    public ConsumerListenerContainer defaultProfilesConsumerListenerContainer(CourierServerProperties properties) {
        Set<String> services = properties.getConsumer().getDefaultProfilesListenServices();
        return new DefaultProfilesConsumerListenerContainer(services);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConsumerListenerContainer consumerListenerContainer() {
        return new ConsumerListenerContainer();
    }

}
