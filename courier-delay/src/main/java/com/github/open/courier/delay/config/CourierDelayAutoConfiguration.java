package com.github.open.courier.delay.config;

import com.github.open.courier.commons.configuration.CourierRepositoryAutoConfiguration;
import com.github.open.courier.commons.configuration.CourierServerBaseAutoConfiguration;
import com.github.open.courier.commons.configuration.CourierRedisAutoConfiguration;
import com.github.open.courier.commons.configuration.RedisBackupAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        CourierServerBaseAutoConfiguration.class,
        CourierRedisAutoConfiguration.class,
        RedisBackupAutoConfiguration.class,
        CourierRepositoryAutoConfiguration.class
})
public class CourierDelayAutoConfiguration {


}
