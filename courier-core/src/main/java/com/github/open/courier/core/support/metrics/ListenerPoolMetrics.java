package com.github.open.courier.core.support.metrics;

import com.github.open.courier.core.listener.AbstractListenerContainer;
import com.github.open.courier.core.listener.ListenerState;
import lombok.Getter;
import lombok.Value;

import java.util.List;
import java.util.stream.Collectors;

/**
 * listener线程池指标, 在基本的线程池指标上, 多了listener的具体信息
 *
 * @author Courier
 */
@Getter
public class ListenerPoolMetrics extends ThreadPoolMetrics {

    final List<Listener> listeners;

    public ListenerPoolMetrics(AbstractListenerContainer listenerContainer) {
        super(listenerContainer.getListenerPool());
        this.listeners = listenerContainer.getListenerMap()
                .values()
                .stream()
                .map(listener -> new Listener(
                        listener.getTopic(),
                        listener.getGroupId(),
                        listener.getListenerSwitch().getKafkaPaused(),
                        listener.getListenerSwitch().getListenerState())
                )
                .collect(Collectors.toList());
    }

    @Value
    static class Listener {

        String topic;
        String groupId;
        Boolean kafkaPaused;
        ListenerState listenerState;
    }
}
