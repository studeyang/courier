package com.github.open.courier.producer.application;

import com.github.open.courier.core.support.Wrapper;
import com.github.open.courier.core.transport.SubscribeMetadata;
import com.github.open.courier.core.transport.SubscribeRequest;
import com.github.open.courier.core.transport.TopicGroup;
import com.github.open.courier.repository.biz.SubscribeBizService;
import com.github.open.courier.repository.biz.bo.SubscribeContext;
import com.github.open.courier.repository.mapper.SubscribeManageMapper;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/6/1
 */
@RunWith(PowerMockRunner.class)
//@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
@PrepareForTest(Wrapper.class)
public class SubscribeAppServiceTest {

    @InjectMocks
    private SubscribeAppService subscribeAppService;

    @Mock
    private SubscribeBizService subscribeBizService;
    @Mock
    private SubscribeManageMapper subscribeManageMapper;
    @Mock
    private RefreshAppService refreshAppService;

    @Test
    public void subscribe_case_insertData() {
        String cluster = "cassmall";
        String env = "kunlun";
        String service = "example-service";
        String url = "http://receive";

        String wrapperTopic = "alpha-example";
        String wrapperGroupId = "ALPHA-GROUP_ID_EXAMPLE";
        String type = "com.xxx.ExampleEvent";

        PowerMockito.mockStatic(Wrapper.class);
        when(Wrapper.wrapTopic("example")).thenReturn(wrapperTopic);
        when(Wrapper.wrapGroupId("GROUP_ID_EXAMPLE")).thenReturn(wrapperGroupId);

        when(subscribeBizService.checkConflict(anyString(), anyString(), anySet())).thenReturn(null);

        when(subscribeManageMapper.queryByService(service)).thenReturn(null);

        when(subscribeBizService.listByService(cluster, service)).thenReturn(new HashSet<>());

        doNothing().when(refreshAppService).refresh();

        SubscribeRequest request = new SubscribeRequest()
                .setCluster(cluster)
                .setEnv(env)
                .setService(service)
                .setDevEnvironment(false)
                .setTopicGroups(Sets.newHashSet(
                        new TopicGroup().setGroupId("GROUP_ID_EXAMPLE").setTopic("example").setTypes(Sets.newHashSet(
                                type))
                ));
        System.out.println(subscribeAppService.subscribe(request));
        // 断言插入了数据
        verify(subscribeBizService).updateSubscribes(SubscribeContext.builder()
                .cluster(cluster)
                .service(service)
                .env(env)
                .build(), Sets.newHashSet(new SubscribeMetadata().setCluster(cluster).setService(service)
                .setTopic(wrapperTopic).setGroupId(wrapperGroupId).setType(type)));
    }

    @Test
    public void subscribe_case_topicGroups_isEmpty() {
        String cluster = "cassmall";
        String env = "kunlun";
        String service = "example-service";

        String wrapperTopic = "alpha-example";
        String wrapperGroupId = "ALPHA-GROUP_ID_EXAMPLE";

        PowerMockito.mockStatic(Wrapper.class);
        when(Wrapper.wrapTopic("example")).thenReturn(wrapperTopic);
        when(Wrapper.wrapGroupId("GROUP_ID_EXAMPLE")).thenReturn(wrapperGroupId);

        when(subscribeBizService.checkConflict(anyString(), anyString(), anySet())).thenReturn(null);

        when(subscribeManageMapper.queryByService(service)).thenReturn(null);

        when(subscribeBizService.listByService(cluster, service)).thenReturn(new HashSet<>());

        doNothing().when(refreshAppService).refresh();

        SubscribeRequest request = new SubscribeRequest()
                .setCluster(cluster)
                .setEnv(env)
                .setService(service)
                .setDevEnvironment(false)
                .setTopicGroups(new HashSet<>());
        System.out.println(subscribeAppService.subscribe(request));
        // 断言执行了更新数据操作
        verify(subscribeBizService, times(0)).updateSubscribes(any(), anySet());
    }

    /**
     * 新增加了一个消息
     */
    @Test
    public void subscribe_case_addAType() {

        PowerMockito.mockStatic(Wrapper.class);
        when(Wrapper.wrapTopic("example")).thenReturn("alpha-example");
        when(Wrapper.wrapGroupId("GROUP_ID_EXAMPLE")).thenReturn("ALPHA-GROUP_ID_EXAMPLE");

        when(subscribeBizService.checkConflict(anyString(), anyString(), anySet())).thenReturn(null);

        when(subscribeManageMapper.queryByService("example-service")).thenReturn(null);

        Set<SubscribeMetadata> subscribeMetadataSet = new HashSet<>();
        subscribeMetadataSet.add(new SubscribeMetadata()
                .setCluster("cassmall")
                .setService("example-service")
                .setTopic("alpha-example")
                .setGroupId("ALPHA-GROUP_ID_EXAMPLE")
                .setType("com.xxx.ExampleEvent")
        );
        when(subscribeBizService.listByService("cassmall", "example-service")).thenReturn(subscribeMetadataSet);

        doNothing().when(refreshAppService).refresh();

        SubscribeRequest request = new SubscribeRequest()
                .setCluster("cassmall")
                .setEnv("kunlun")
                .setService("example-service")
                .setDevEnvironment(false)
                .setTopicGroups(new HashSet() {{
                    add(new TopicGroup().setGroupId("GROUP_ID_EXAMPLE").setTopic("example")
                            .setTypes(new HashSet() {{
                                add("com.xxx.ExampleEvent");
                                add("com.xxx.ExampleEvent1");
                            }}));
                }});
        System.out.println(subscribeAppService.subscribe(request));

//        verify(subscribeBizService).insertBatch(anySet());
    }

    @Test
    public void isEqualCollection() {
        String cluster = "cassmall";
        String env = "kunlun";
        String service = "example-service";
        String topic = "TOPIC";
        String groupId = "GROUPID";
        String type = "com.xxx.UserCreated";

        Set<SubscribeMetadata> oldSubscribes = new HashSet<>();
        oldSubscribes.add(new SubscribeMetadata()
                .setCluster(cluster)
                .setService(service)
                .setTopic(topic)
                .setGroupId(groupId)
                .setType(type));

        Set<SubscribeMetadata> newSubscribes = new HashSet<>();
        newSubscribes.add(new SubscribeMetadata()
                .setCluster(cluster)
                .setEnv(env) // 相对于 oldSubscribes 多了这个字段
                .setService(service)
                .setTopic(topic)
                .setGroupId(groupId)
                .setType(type));

        Assert.assertTrue(CollectionUtils.isEqualCollection(oldSubscribes, newSubscribes));
    }

}