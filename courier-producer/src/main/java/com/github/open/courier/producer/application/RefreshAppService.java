package com.github.open.courier.producer.application;

import com.github.open.courier.core.converter.MessageJsonConverter;
import com.github.open.courier.core.message.subscribe.RefreshListenerSubscribe;
import com.github.open.courier.core.support.Wrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;

import static com.github.open.courier.core.constant.MessageConstant.SUBSCRIBE_TOPIC;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/5/31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshAppService {

    private final Producer<String, String> producer;

    public void refresh() {

        RefreshListenerSubscribe subscribe = new RefreshListenerSubscribe();

        String jsonSubscribe = MessageJsonConverter.toJson(subscribe);

        ProducerRecord<String, String> refreshRecord = new ProducerRecord<>(Wrapper.wrapTopic(SUBSCRIBE_TOPIC), jsonSubscribe);

        log.info("发送刷新listener的消息...");
        try {
            producer.send(refreshRecord).get();
        } catch (Exception e) {
            log.error("刷新listener异常", e);
            Thread.currentThread().interrupt();
        }
    }

}
