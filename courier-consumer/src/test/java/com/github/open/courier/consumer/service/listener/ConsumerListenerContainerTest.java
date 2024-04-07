package com.github.open.courier.consumer.service.listener;

import com.github.open.courier.core.transport.SubscribeMetadata;
import com.github.open.courier.repository.biz.SubscribeBizService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/6/9
 */
@RunWith(MockitoJUnitRunner.class)
public class ConsumerListenerContainerTest {

    @InjectMocks
    private ConsumerListenerContainer consumerListenerContainer;

    @Mock
    private SubscribeBizService subscribeBizService;

    @Test
    public void listConsumerGroup() {
        String service = "example-service";
        String topic = "example";
        String groupId = "GROUP_ID";

        List<SubscribeMetadata> list = new ArrayList<>();
        list.add(new SubscribeMetadata().setTopic(topic).setGroupId(groupId).setService(service)
                .setType("com.xxx.Event"));
        list.add(new SubscribeMetadata().setTopic(topic).setGroupId(groupId).setService(service)
                .setType("com.xxx.Event1"));
        when(subscribeBizService.listEnableService()).thenReturn(list);

        Assert.assertEquals(2, consumerListenerContainer.listConsumerGroup().stream()
                .filter(cg -> groupId.equals(cg.getGroupId()))
                .findFirst()
                .map(ConsumerListenerContainer.ConsumerGroup::getTypes)
                .get()
                .size());
    }

}