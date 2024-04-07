package com.github.open.courier.commons.configuration;

import com.github.open.courier.commons.redis.RedisHelper;
import com.github.open.courier.repository.mapper.MessageMapper;
import com.github.open.courier.commons.redis.backup.MessageBackupTaskSupport;
import com.github.open.courier.commons.support.CourierServerProperties;
import com.github.open.courier.core.constant.MessageConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/6/22
 */
public class RedisBackupAutoConfiguration {

    /**
     * 消费备份入库线程池
     */
    @Bean(MessageConstant.DB_SERVICE_EXECUTOR_NAME)
    public ExecutorService dbServiceExecutor(CourierServerProperties properties) {
        return Executors.newFixedThreadPool(properties.getProducer().getBackupTaskCount(),
                new CustomizableThreadFactory("redis-backup-task-"));
    }

    @Bean
    public MessageBackupTaskSupport messageBackupTaskSupport(ExecutorService dbServiceExecutor,
                                                             MessageMapper messageMapper,
                                                             RedisHelper redisHelper,
                                                             CourierServerProperties properties) {
        return new MessageBackupTaskSupport(dbServiceExecutor,
                messageMapper,
                redisHelper,
                properties.getProducer().getBackupTaskCount());
    }

}
