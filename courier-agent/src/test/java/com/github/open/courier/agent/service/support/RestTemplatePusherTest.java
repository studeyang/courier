package com.github.open.courier.agent.service.support;

import com.github.open.courier.agent.infrastructure.config.CourierAgentProperties;
import com.github.open.courier.commons.support.ServiceDiscovery;
import com.github.open.courier.core.transport.ConsumeMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/8/2
 */
@RunWith(MockitoJUnitRunner.class)
public class RestTemplatePusherTest {

    @InjectMocks
    private RestTemplatePusher restTemplatePusher;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ServiceDiscovery serviceDiscovery;
    @Mock
    private CourierAgentProperties properties;

    @Test
    public void pushByService() {

        String service = "example-service";
        List<ConsumeMessage> messages = new ArrayList<>();
        messages.add(new ConsumeMessage());
        messages.add(new ConsumeMessage());
        messages.add(new ConsumeMessage().setPrimaryKey("0826"));
        messages.add(new ConsumeMessage().setPrimaryKey("0826"));
        messages.add(new ConsumeMessage().setPrimaryKey("0827"));

        List<String> hosts = new ArrayList<>();
        hosts.add("10.0.0.1:8080");
        hosts.add("10.0.0.2:8080");
        hosts.add("10.0.0.3:8080");
        when(serviceDiscovery.getServiceHostAndPort(service)).thenReturn(hosts);

        when(restTemplate.postForEntity(anyString(), anyList(), any())).thenReturn(null);

        restTemplatePusher.pushByService(service, messages);

        verify(restTemplate, times(3)).postForEntity(anyString(), anyList(), any());
    }

}