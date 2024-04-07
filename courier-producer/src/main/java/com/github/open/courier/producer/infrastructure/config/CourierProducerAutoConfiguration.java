package com.github.open.courier.producer.infrastructure.config;

import com.github.open.courier.commons.configuration.CourierRedisAutoConfiguration;
import com.github.open.courier.commons.configuration.CourierRepositoryAutoConfiguration;
import com.github.open.courier.commons.configuration.CourierServerBaseAutoConfiguration;
import com.github.open.courier.commons.configuration.RedisBackupAutoConfiguration;
import com.google.common.collect.Maps;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Map;

@Configuration
@Import({
        CourierServerBaseAutoConfiguration.class,
        CourierRedisAutoConfiguration.class,
        RedisBackupAutoConfiguration.class,
        CourierRepositoryAutoConfiguration.class
})
public class CourierProducerAutoConfiguration {

    @Bean(destroyMethod = "close")
    public Producer<String, String> kafkaProducer(@Value("${courier.brokers}") String brokers) {

        Map<String, Object> props = Maps.newHashMap();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16 * 1024);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 32 * 1024 * 1024);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 3000);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);

        return new KafkaProducer<>(props);
    }

}
