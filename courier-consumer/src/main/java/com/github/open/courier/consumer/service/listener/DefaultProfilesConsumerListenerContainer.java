package com.github.open.courier.consumer.service.listener;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/6/23
 */
public class DefaultProfilesConsumerListenerContainer extends ConsumerListenerContainer {

    private final Set<String> services;

    public DefaultProfilesConsumerListenerContainer(Set<String> services) {
        this.services = services;
    }

    @Override
    List<ConsumerGroup> listConsumerGroup() {
        return super.listConsumerGroup()
                .stream()
                .filter(consumerGroup -> services.contains(consumerGroup.getService()))
                .collect(Collectors.toList());
    }

}
