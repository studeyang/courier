package com.github.open.courier.consumer.listener.internal;

import com.github.open.courier.commons.support.AssignConsume;
import com.github.open.courier.core.support.CourierContext;
import com.github.open.courier.repository.mapper.SubscribeManageMapper;
import com.github.open.courier.commons.loadbalance.RandomLoadBalance;
import com.github.open.courier.core.transport.SubscribeManageDTO;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssignConsumeTest {

    @Mock
    private SubscribeManageMapper subscribeManageMapper;

    @Mock
    private RandomLoadBalance randomLoadBalance;

    @InjectMocks
    private AssignConsume assignConsume;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private Environment environment;

    @Before
    public void before() {

        when(environment.getProperty(anyString())).thenReturn("123");
        when(applicationContext.getEnvironment()).thenReturn(environment);

        CourierContext courierContext = new CourierContext();
        courierContext.setApplicationContext(applicationContext);

    }

    @Test
    public void assignIfNecessary() {
        //没有指定，返回随机策略的节点
        when(subscribeManageMapper.queryByService(anyString())).thenReturn(new SubscribeManageDTO());
        when(randomLoadBalance.choose(anyList())).thenReturn("1.1.1.1:8888");
        Assert.assertEquals("1.1.1.1:8888", assignConsume.assignIfNecessary(Lists.newArrayList(), "service-name"));

        //有指定，返回指定的节点
        when(subscribeManageMapper.queryByService(anyString())).thenReturn(new SubscribeManageDTO().setConsumerNode("0.0.0.0:7777"));
        Assert.assertEquals("0.0.0.0:7777", assignConsume.assignIfNecessary(Lists.newArrayList(), "service-name"));

    }


}
