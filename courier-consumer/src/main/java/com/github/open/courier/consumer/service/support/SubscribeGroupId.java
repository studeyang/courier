package com.github.open.courier.consumer.service.support;

import com.github.open.courier.core.support.CourierContext;
import com.github.open.courier.repository.mapper.SubscribeGroupIdMapper;
import com.github.open.courier.core.exception.NotActiveGroupIdException;
import com.github.open.courier.core.support.Wrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import static com.github.open.courier.core.constant.MessageConstant.COURIER_CONTEXT_BEAN;

/**
 * 订阅关系消息listener的groupId
 */
@Slf4j
@Component
@DependsOn(COURIER_CONTEXT_BEAN)
public class SubscribeGroupId {

    @Autowired
    private SubscribeGroupIdMapper subscribeGroupIdMapper;

    private static Integer id = 1;
    private static String holder;

    /**
     * 初始化时从数据库拿到唯一的groupId
     */
    @PostConstruct
    private void init() throws UnknownHostException {

        // eg. courier-consumer/10.44.2.9:11112
        // NOSONAR, 单例只会set一次, sonar误报, 下同
        holder = CourierContext.getService()
                + '/'
                + InetAddress.getLocalHost().getHostAddress()
                + ':'
                + CourierContext.getProperty("server.port");

        /*
         * 服务重启的时候，是先把镜像删掉，然后再创建新的理论上每次重启 ip 都是会改变的
         * 1丶服务重启的时候，是两个节点，是依次重启两个节点所以不需要考虑并发问题
         * 2丶目前该服务只有两个节点，kafka 上有二十多个对应的 consumerGroup ，先考虑使用前 20 个 id，如果后续需要优化的时候再考虑动态添加 consumer 到阿里云 kafka
         * 3丶从 id = 1 的节点开始添加，以 id 为准
         */
        checkId();
    }

    /**
     * 一个 id 对应一个 consumerGroup
     */
    private void checkId() {
        // 目前在阿里云上旧的只创建了 20 个相关的消费组
        if (id > 20) {
            throw new NotActiveGroupIdException();
        }

        String oldId = subscribeGroupIdMapper.selectIdById(id);
        if (StringUtils.isNotEmpty(oldId)) {
            id++;
            checkId();
        } else {
            insertOrUpdate();
        }
    }

    private void insertOrUpdate() {

        int row = subscribeGroupIdMapper.update(id, 1, holder, new Date());

        if (row == 0) {
            subscribeGroupIdMapper.insert(id, 1, holder, new Date());
        }
    }

    /**
     * 在结束时释放持有的groupId
     * 在kill -9, 及服务突然暴毙的情况下(确实出现过), 将无法释放groupId，突然暴毙是不会执行 release 方法的，只能手动释放，这里不需要处理
     */
    @PreDestroy
    public void release() {
        log.info("开始释放groupId, id:{}, holder:{}", id, holder);
        try {
            // 释放改为直接删除，免得占用资源
            subscribeGroupIdMapper.release(id, holder);
        } catch (Exception e) {
            log.warn("释放groupId失败, id:{}, holder:{}", id, holder, e);
        }
    }

    /**
     * 获取groupId
     */
    public static String get() {
        return Wrapper.wrapGroupId("consumer-subscribe-" + id);
    }
}
