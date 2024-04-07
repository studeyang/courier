package com.github.open.courier.management.service;

import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.transport.SubscribeManageRequest;
import com.github.open.courier.core.transport.SubscribeManageDTO;
import com.github.open.courier.repository.mapper.SubscribeManageMapper;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminServiceTest {

    @LocalServerPort
    private String port;

    private final RestTemplate restTemplate = new RestTemplate();

    private final String URL = "http://localhost:";

    @Autowired
    private SubscribeManageMapper subscribeManageMapper;

    /**
     * 请求全部订阅管理数据
     * |-断言请求返回状态200
     */
    @Test
    @Ignore
    public void testQueryAllSubscribePageDTO() {
        String url = URL + port + URLConstant.MANAGEMENT_QUERY_ALL_SUBSCRIBE_MANAGE;
        try {
            ResponseEntity<Object> exchange = restTemplate.exchange(url, HttpMethod.POST, null, Object.class);
            Assert.assertEquals("接口调用成功", exchange.getStatusCodeValue(), 200);
        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail("不应该执行到这。");
        }
    }

    /**
     * 请求单个订阅管理数据
     * |-断言请求返回状态200
     */
    @Test
    @Ignore
    public void testQueryOneSubscribePageDTO() {
        String url = URL + port + URLConstant.MANAGEMENT_QUERY_SUBSCRIBE_MANAGE;
        try {
            ResponseEntity<Object> exchange = restTemplate.postForEntity(url, "user-service", Object.class);
            Assert.assertEquals("接口调用成功", exchange.getStatusCodeValue(), 200);
        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail("不应该执行到这。");
        }
    }

    /**
     * 指定消费节点
     * |-断言请求返回状态200
     * |-断言请求返回指定的消费节点
     */
    @Test
    @Ignore
    public void testSubscribeBind() {
        String url = URL + port + URLConstant.MANAGEMENT_SUBSCRIBE_BIND;
        String node = "0.0.0.0:111";
        try {
            ResponseEntity<Object> exchange = restTemplate.postForEntity(url,
                    new SubscribeManageRequest().setService("===test-service====").setConsumerNode(node), Object.class);
            Assert.assertEquals("接口调用成功", exchange.getStatusCodeValue(), 200);
            SubscribeManageDTO subscribeManageDTO = subscribeManageMapper.queryByService("===test-service====");
            Assert.assertEquals("指定消费节点成功", subscribeManageDTO.getConsumerNode(), node);

        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail("不应该执行到这。");
        }
    }

}
