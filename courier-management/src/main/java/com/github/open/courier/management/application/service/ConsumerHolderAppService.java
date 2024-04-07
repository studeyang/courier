package com.github.open.courier.management.application.service;

import com.github.open.courier.commons.support.CourierDiscoveryClient;
import com.github.open.courier.core.transport.SubscribeGroupId;
import com.github.open.courier.repository.mapper.SubscribeGroupIdMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConsumerHolderAppService {

    private static final String COURIER_CONSUMER_SERVICE = "courier-consumer";

    @Autowired
    private CourierDiscoveryClient courierDiscoveryClient;

    @Autowired
    private SubscribeGroupIdMapper subscribeGroupIdMapper;


    public void groupIdSurviveCheck() {

        List<String> ipPortList = courierDiscoveryClient.getServiceHostAndPort(COURIER_CONSUMER_SERVICE);

        Set<String> realHolders = ipPortList.stream()
                .map(ipPort -> COURIER_CONSUMER_SERVICE + "/" + ipPort).collect(Collectors.toSet());

        List<Integer> excludeIds = subscribeGroupIdMapper.list().stream()
                .filter(groupId -> !realHolders.contains(groupId.getHolder()))
                .map(SubscribeGroupId::getId)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(excludeIds)) {
            subscribeGroupIdMapper.deleteByIds(excludeIds);
        }
    }

}
