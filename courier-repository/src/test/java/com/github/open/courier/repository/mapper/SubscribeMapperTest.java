package com.github.open.courier.repository.mapper;

import com.github.open.courier.repository.config.MyBatisTestConfig;
import com.github.open.courier.repository.entity.SubscribeEntity;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/5/31
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = MyBatisTestConfig.class)
public class SubscribeMapperTest {

    @Autowired
    private SubscribeMapper mapper;

    @Test
    public void setUp() {

    }

    private void initOneData() {
        List<SubscribeEntity> subscribeEntities = new ArrayList<>();
        subscribeEntities.add(new SubscribeEntity()
                .setCluster("cassmall").setService("example-service").setTopic("alpha-test")
                .setGroupId("test-groupId").setType("com.xxx.TestEvent").setEnvTag("kunlun"));
        mapper.insertBatch(subscribeEntities);
    }

    @Test
    public void insertBatch() {
        List<SubscribeEntity> subscribeEntities = new ArrayList<>();
        subscribeEntities.add(new SubscribeEntity()
                .setService("example-service").setTopic("alpha-test").setGroupId("test-groupId")
                .setType("com.xxx.TestEvent")
        );
        Assert.assertEquals(1, mapper.insertBatch(subscribeEntities));
    }

    @Test
    public void deleteByClusterAndService() {
        initOneData();
        Assert.assertEquals(1, mapper.deleteByClusterAndService("cassmall", "example-service"));
    }

    @Test
    public void listByService() {
        initOneData();
        Assert.assertEquals(1, mapper.listByService("cassmall", "example-service").size());
    }

    @Test
    public void listAll() {
        initOneData();
        Assert.assertEquals(1, mapper.listAll().size());
    }

    @Test
    public void countCluster() {
        initOneData();
        Assert.assertEquals(1, mapper.countCluster("cassmall"));
    }

    @Test
    public void selectEnvTag() {
        initOneData();
        Assert.assertEquals(1, mapper.selectEnvTag("cassmall", "example-service").size());
    }

    @Test
    public void whereCluster() {
        initOneData();
        Assert.assertEquals("cassmall", mapper.whereCluster("test-groupId"));
    }



    @After
    public void tearDown() {
        mapper.deleteAll();
    }

}