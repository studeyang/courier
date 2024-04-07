package com.github.open.courier.consumer.listener.internal;

import com.github.open.courier.commons.redis.RedisClient;
import com.github.open.courier.commons.support.AssignConsume;
import com.github.open.courier.commons.support.CourierDiscoveryClient;
import com.github.open.courier.core.support.CourierContext;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.net.URI;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CourierDiscoveryClientTest {

    @InjectMocks
    private CourierDiscoveryClient courierDiscoveryClient;

    @Mock
    private DiscoveryClient discoveryClient;

    @Mock
    private RedisClient redisClient;

    @Mock
    private AssignConsume assignConsume;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private Environment environment;

    @Before
    public void before() {
//        courierDiscoveryClient.afterPropertiesSet();

        when(environment.getProperty(anyString())).thenReturn("123");
        when(applicationContext.getEnvironment()).thenReturn(environment);

        CourierContext courierContext = new CourierContext();
        courierContext.setApplicationContext(applicationContext);
    }

    @Test
    public void choose() {

        TestServiceInstance instance1 = new TestServiceInstance("10.0.0.1", 8080);
        TestServiceInstance instance2 = new TestServiceInstance("10.0.0.2", 8080);
        TestServiceInstance instance3 = new TestServiceInstance("10.0.0.3", 8080);

        when(discoveryClient.getInstances(anyString()))
                .thenReturn(Lists.newArrayList(instance1, instance2, instance3));

        when(redisClient.listAll(anyString()))
                .thenReturn(Lists.newArrayList("10.0.0.1:8080"));

        when(assignConsume.assignIfNecessary(anyList(), anyString())).thenReturn("10.0.0.1:8080");

        Assert.assertEquals("10.0.0.1:8080", courierDiscoveryClient.choose("courier-example"));
    }

    @Test
    public void getServiceUrl() {

        TestServiceInstance instance1 = new TestServiceInstance("10.0.0.1", 8080);
        TestServiceInstance instance2 = new TestServiceInstance("10.0.0.2", 8080);
        TestServiceInstance instance3 = new TestServiceInstance("10.0.0.3", 8080);

        when(discoveryClient.getInstances(anyString()))
                .thenReturn(Lists.newArrayList(instance1, instance2, instance3));

        Assert.assertEquals("http://10.0.0.1:8080", courierDiscoveryClient.getServiceUrl("courier-example").get(0));
    }

    @Test
    public void getServiceHostAndPort() {

        TestServiceInstance instance1 = new TestServiceInstance("10.0.0.1", 8080);
        TestServiceInstance instance2 = new TestServiceInstance("10.0.0.2", 8080);
        TestServiceInstance instance3 = new TestServiceInstance("10.0.0.3", 8080);

        when(discoveryClient.getInstances(anyString()))
                .thenReturn(Lists.newArrayList(instance1, instance2, instance3));

        Assert.assertEquals("10.0.0.1:8080", courierDiscoveryClient.getServiceHostAndPort("courier-example").get(0));
        verify(discoveryClient, times(1)).getInstances("courier-example");
    }

    @Test
    @Ignore //去掉了k8s服务发现
    public void getServiceHostAndPort_when_emptyListK8s() {

        when(discoveryClient.getInstances(anyString()))
                .thenReturn(Lists.newArrayList());

        TestServiceInstance instance1 = new TestServiceInstance("10.0.0.1", 8080);

        when(discoveryClient.getInstances(anyString()))
                .thenReturn(Lists.newArrayList(instance1));

        String service = "courier-example";

        courierDiscoveryClient.getServiceHostAndPort(service);
        courierDiscoveryClient.getServiceHostAndPort(service);
        courierDiscoveryClient.getServiceHostAndPort(service);

        verify(discoveryClient, times(1)).getInstances(service);
    }

    @Test //去掉了k8s服务发现
    @Ignore
    public void getServiceHostAndPort_when_emptyListK8s_and_emptyListEureka() {

        String service = "courier-example";
        String service2 = "courier-example2";

        when(discoveryClient.getInstances(anyString()))
                .thenReturn(Lists.newArrayList());

        when(discoveryClient.getInstances(service))
                .thenReturn(Lists.newArrayList());

        TestServiceInstance instance1 = new TestServiceInstance("10.0.0.1", 8080);

        when(discoveryClient.getInstances(service2))
                .thenReturn(Lists.newArrayList(instance1));

        courierDiscoveryClient.getServiceHostAndPort(service);
        courierDiscoveryClient.getServiceHostAndPort(service);
        courierDiscoveryClient.getServiceHostAndPort(service);

        courierDiscoveryClient.getServiceHostAndPort(service2);
        courierDiscoveryClient.getServiceHostAndPort(service2);

        verify(discoveryClient, times(3)).getInstances(service);
        verify(discoveryClient, times(1)).getInstances(service2);
    }

    @Test
    public void getServiceUrlWithMessagesReceive() {

        TestServiceInstance instance1 = new TestServiceInstance("10.0.0.1", 8080);
        TestServiceInstance instance2 = new TestServiceInstance("10.0.0.2", 8080);
        TestServiceInstance instance3 = new TestServiceInstance("10.0.0.3", 8080);

        when(discoveryClient.getInstances(anyString()))
                .thenReturn(Lists.newArrayList(instance1, instance2, instance3));

        Assert.assertEquals("http://10.0.0.1:8080/courier/messages/receive",
                courierDiscoveryClient.getServiceUrlWithMessagesReceive("courier-example").get(0));
    }

    private static class TestServiceInstance implements ServiceInstance {

        private String host;
        private int port;

        TestServiceInstance(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public String getServiceId() {
            return null;
        }

        @Override
        public String getHost() {
            return host;
        }

        @Override
        public int getPort() {
            return port;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public URI getUri() {
            return null;
        }

        @Override
        public Map<String, String> getMetadata() {
            return Maps.newHashMap();
        }
    }

}