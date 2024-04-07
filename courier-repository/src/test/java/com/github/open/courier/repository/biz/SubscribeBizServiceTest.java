package com.github.open.courier.repository.biz;

import com.github.open.courier.core.transport.SubscribeMetadata;
import com.github.open.courier.repository.biz.bo.SubscribeContext;
import com.github.open.courier.repository.entity.SubscribeEntity;
import com.github.open.courier.repository.mapper.SubscribeMapper;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.Mockito.*;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/6/6
 */
@RunWith(MockitoJUnitRunner.class)
public class SubscribeBizServiceTest {

    @InjectMocks
    private SubscribeBizService subscribeBizService;
    @Mock
    private SubscribeMapper subscribeMapper;

    @Test
    public void listByService() {
        String cluster = "cassmall";
        String service = "example-service";

        Set<SubscribeEntity> subscribeEntities = new HashSet<>();
        subscribeEntities.add(new SubscribeEntity().setCluster(cluster).setService(service).setType("com.github.Event"));
        subscribeEntities.add(new SubscribeEntity().setCluster(cluster).setService(service).setType("com.github.Event1").setEnvTag("kunlun"));
        subscribeEntities.add(new SubscribeEntity().setCluster(cluster).setService(service).setType("com.github.Event2").setEnvTag("penglai"));
        when(subscribeMapper.listByService(cluster, service)).thenReturn(subscribeEntities);

        Set<SubscribeMetadata> metadatas = subscribeBizService.listByService(cluster, service);

        Assert.assertEquals(3, metadatas.size());
    }

    @Test
    public void insertBatch() {
        String cluster = "cassmall";
        String service = "example-service";
        String env = "kunlun";
        String url = "http://mall-agent-kunlun";

        Set<SubscribeMetadata> subscribeMetadatas = new HashSet<>();
        subscribeMetadatas.add(new SubscribeMetadata()
                .setCluster(cluster).setEnv(env).setService(service)
                .setGroupId("GROUPID").setTopic("topic").setType("com.github.UserCreated"));

        subscribeBizService.insertBatch(Collections.emptyMap(), subscribeMetadatas);

        Set<SubscribeEntity> subscribeEntities = new HashSet<>();
        subscribeEntities.add(new SubscribeEntity()
                .setCluster(cluster).setService(service).setTopic("topic")
                .setGroupId("GROUPID").setType("com.github.UserCreated"));
        verify(subscribeMapper).insertBatch(subscribeEntities);
    }

    @Test
    public void updateSubscribes() {
        String cluster = "cassmall";
        String service = "example-service";
        String topic = "user";
        String type = "com.github.UserCreated";
        String env = "kunlun";
        String groupId = "GROUPID";

        List<SubscribeEntity> subscribeEntities = new ArrayList<>();
        subscribeEntities.add(new SubscribeEntity().setTopic(topic).setType(type).setEnvTag(env));
        when(subscribeMapper.selectEnvTag(cluster, service)).thenReturn(subscribeEntities);

        mockDeleteSubscribes();

        Set<SubscribeMetadata> subscribeMetadatas = new HashSet<>();
        subscribeMetadatas.add(new SubscribeMetadata()
                .setCluster(cluster).setEnv(env).setService(service)
                .setGroupId(groupId).setTopic(topic).setType(type));
        subscribeBizService.updateSubscribes(
                SubscribeContext.builder().cluster(cluster).service(service).build(), subscribeMetadatas);

        verify(subscribeMapper).insertBatch(Sets.newHashSet(new SubscribeEntity()
                .setCluster(cluster).setService(service).setTopic(topic)
                .setGroupId(groupId).setType(type).setEnvTag(env)));
    }

    private void mockDeleteSubscribes() {
        when(subscribeMapper.deleteByClusterAndService(anyString(), anyString())).thenReturn(0);
        when(subscribeMapper.countCluster(anyString())).thenReturn(1);
    }

}