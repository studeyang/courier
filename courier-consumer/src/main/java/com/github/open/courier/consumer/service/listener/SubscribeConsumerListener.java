package com.github.open.courier.consumer.service.listener;

import com.github.open.courier.core.converter.MessageJsonConverter;
import com.github.open.courier.core.listener.AbstractConsumerListener;
import com.github.open.courier.core.support.CourierContext;
import com.github.open.courier.consumer.service.biz.PushBizService;
import com.github.open.courier.consumer.service.support.SubscribeClusterContainer;
import com.github.open.courier.consumer.service.support.SubscribeGroupId;
import com.github.open.courier.core.listener.AbstractListenerContainer;
import com.github.open.courier.core.listener.ListenerConfig;
import com.github.open.courier.core.message.Subscribe;
import com.github.open.courier.core.message.subscribe.PausePushSubscribe;
import com.github.open.courier.core.message.subscribe.RefreshListenerSubscribe;
import com.github.open.courier.core.message.subscribe.ResumePushSubscribe;
import com.github.open.courier.core.support.Wrapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import static com.github.open.courier.core.constant.MessageConstant.SUBSCRIBE_TOPIC;

/**
 * 订阅关系变化消息的listener
 *
 * @author Courier
 */
@Slf4j
public final class SubscribeConsumerListener extends AbstractConsumerListener {

    public static final SubscribeConsumerListener INSTANCE = new SubscribeConsumerListener();

    private final AbstractListenerContainer listenerContainer;

    private final PushBizService pushBizService;

    private final SubscribeClusterContainer subscribeClusterContainer;

    private SubscribeConsumerListener() {
        super(SubscribeListenerConfig.INSTANCE);
        this.listenerContainer = CourierContext.getBean(AbstractListenerContainer.class);
        this.pushBizService = CourierContext.getBean(PushBizService.class);
        this.subscribeClusterContainer = CourierContext.getBean(SubscribeClusterContainer.class);
    }

    /**
     * 该listener的配置
     */
    @Getter
    enum SubscribeListenerConfig implements ListenerConfig {

        /**
         * 实例
         */
        INSTANCE;

        String topic = Wrapper.wrapTopic(SUBSCRIBE_TOPIC);
        String groupId = SubscribeGroupId.get();
    }

    @Override
    protected void pauseOrResumeIfNecessary(Consumer<String, String> consumer) {
        // do nothing
    }

    /**
     * 接收订阅关系变化的消息
     */
    @Override
    public void handle(ConsumerRecords<String, String> records) {

        for (ConsumerRecord<String, String> record : records) {

            Subscribe subscribe = MessageJsonConverter.toObject(record.value(), Subscribe.class);

            if (subscribe instanceof RefreshListenerSubscribe) {
                log.info("refreshing...");
                listenerContainer.refresh();
                subscribeClusterContainer.refresh();
            } else if (subscribe instanceof PausePushSubscribe) {
                log.info("pause push");
                pushBizService.pauseKafkaIfNecessary((PausePushSubscribe) subscribe);
            } else if (subscribe instanceof ResumePushSubscribe) {
                log.info("resume push");
                pushBizService.resumeKafkaIfNecessary((ResumePushSubscribe) subscribe);
            }
        }
    }

}
