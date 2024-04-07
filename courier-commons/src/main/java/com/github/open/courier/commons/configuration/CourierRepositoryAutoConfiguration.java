package com.github.open.courier.commons.configuration;

import com.github.open.courier.repository.mapper.SubscribeMapper;
import com.github.open.courier.repository.biz.SubscribeBizService;
import com.github.open.courier.repository.configuration.MyBatisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/6/23
 */
@Import({MyBatisAutoConfiguration.class})
public class CourierRepositoryAutoConfiguration {

    @Bean
    public SubscribeBizService subscribeBizService(SubscribeMapper subscribeMapper) {
        return new SubscribeBizService(subscribeMapper);
    }

}
