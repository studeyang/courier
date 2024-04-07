package com.github.open.courier.consumer.service.listener;

import com.github.open.courier.consumer.service.support.NacosDiscoverySupport;
import com.github.open.courier.consumer.service.support.RestTemplatePusher;
import com.github.open.courier.core.transport.ConsumeMessage;
import com.github.open.courier.repository.mapper.ConsumeRecordMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PushConsumerListenerTest {

    @Mock
    private ConsumerListenerContainer.ConsumerGroup consumerGroup;
    @Mock
    private ConsumeRecordMapper consumeRecordMapper;
    @Mock
    private RestTemplatePusher restTemplatePusher;
    @Mock
    private NacosDiscoverySupport nacosDiscoverySupport;

    private PushConsumerListener newInstance() {
        return new PushConsumerListener(consumerGroup, consumeRecordMapper, restTemplatePusher,
                nacosDiscoverySupport);
    }

    @Test
    public void groupByUrl() {
        String toCluster = "cassmall";
        String groupId = "EXAMPLE";
        String type1 = "com.xxx.UserCrated1";
        String type2 = "com.xxx.UserCrated2";

        Map<String, String> types = new HashMap<>();
        types.put(type1, "kunlun");
        types.put(type2, "penglai");

        when(consumerGroup.getCluster()).thenReturn(toCluster);
        when(consumerGroup.getGroupId()).thenReturn(groupId);
        when(consumerGroup.getTypes()).thenReturn(types);

        when(nacosDiscoverySupport.selectUrl(toCluster, "kunlun")).thenReturn("10.0.0.1:8080");
        when(nacosDiscoverySupport.selectUrl(toCluster, "penglai")).thenReturn("10.0.0.2:8080");

        List<ConsumeMessage> messages = new ArrayList<>();
        messages.add(new ConsumeMessage().setType(type1));
        messages.add(new ConsumeMessage().setType(type2));
        messages.add(new ConsumeMessage());
        messages.add(new ConsumeMessage());
        messages.add(new ConsumeMessage());

        Assert.assertEquals(3, newInstance().groupByUrl(messages).size());
    }


}