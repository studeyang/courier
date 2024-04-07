package com.github.open.courier.consumer.service.biz;

import com.github.open.courier.commons.redis.RedisClient;
import com.github.open.courier.consumer.service.listener.ConsumerListenerContainer;
import com.github.open.courier.core.constant.MessageConstant;
import com.github.open.courier.core.message.subscribe.PausePushSubscribe;
import com.github.open.courier.core.message.subscribe.ResumePushSubscribe;
import com.github.open.courier.core.transport.PushOperationRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 暂停/恢复推送服务类
 *
 * @author yangllu
 */
@Service
public class PushBizService {

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private ConsumerListenerContainer listenerContainer;

    /**
     * 暂停推送
     *
     * @param request 暂停推送请求
     */
    public void pausePush(PushOperationRequest request) {

        PausePushSubscribe subscribe = new PausePushSubscribe()
                .setIp(request.getIp())
                .setPort(request.getPort())
                .setService(request.getService());

        pauseKafkaIfNecessary(subscribe);
    }

    /**
     * 恢复推送
     *
     * @param request 恢复推送请求
     */
    public void resumePush(PushOperationRequest request) {

        ResumePushSubscribe subscribe = new ResumePushSubscribe()
                .setIp(request.getIp())
                .setPort(request.getPort())
                .setService(request.getService());

        resumeKafkaIfNecessary(subscribe);
    }

    /**
     * 暂停操作
     *
     * @param subscribe 暂停消息
     */
    public void pauseKafkaIfNecessary(PausePushSubscribe subscribe) {

        String redisKey = String.format(MessageConstant.PAUSE_LIST, subscribe.getService());

        // 所有节点是否都暂停了？

        List<String> allPods = new ArrayList<>();
        List<String> pausedPods = redisClient.listAll(redisKey);

        List<String> availablePods = ListUtils.subtract(allPods, pausedPods);

        if (CollectionUtils.isEmpty(availablePods)) {
            // 向 Kafka 发送 pause 请求
            listenerContainer.tryPause(subscribe.getService());
        }

        List<String> deadPods = ListUtils.subtract(pausedPods, allPods);

        if (CollectionUtils.isNotEmpty(deadPods)) {
            // 剔除 redis 中已经死亡的节点
            deadPods.forEach(pod -> redisClient.remove(redisKey, pod));
        }
    }

    /**
     * 恢复操作
     *
     * @param subscribe 恢复消息
     */
    public void resumeKafkaIfNecessary(ResumePushSubscribe subscribe) {

        // kafka是否处于暂停状态？

        listenerContainer.tryResume(subscribe.getService());
    }

}
