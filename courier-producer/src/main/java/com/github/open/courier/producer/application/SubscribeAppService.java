package com.github.open.courier.producer.application;

import com.github.open.courier.core.converter.MessageJsonConverter;
import com.github.open.courier.core.support.Wrapper;
import com.github.open.courier.core.transport.*;
import com.github.open.courier.repository.biz.SubscribeBizService;
import com.github.open.courier.repository.biz.bo.SubscribeContext;
import com.github.open.courier.repository.mapper.SubscribeManageMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.open.courier.core.transport.SubscribeResult.error;
import static java.text.MessageFormat.format;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/5/31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscribeAppService {

    private static final SubscribeResult SUCCESS = SubscribeResult.success("kafka订阅消息成功");

    private final SubscribeBizService subscribeBizService;
    private final SubscribeManageMapper subscribeManageMapper;
    private final RefreshAppService refresher;

    public SubscribeResult subscribe(SubscribeRequest request) {

        log.info("开始订阅消息, request:{}", MessageJsonConverter.toJson(request));

        SubscribeResult errorResult = validateAndWrap(request);
        if (errorResult != null) {
            log.warn("订阅失败!!! reason: {}", errorResult.getReason());
            return errorResult;
        }

        /*
         * 以下情况的服务订阅需要做特殊处理：
         * 1.该服务指定了消费节点； 2.xxx；
         */
        SubscribeManageDTO serviceManage = subscribeManageMapper.queryByService(request.getService());
        if (serviceManage != null && !serviceManage.isEnable()) {
            serviceManage.setEnable(true);
            subscribeManageMapper.update(serviceManage);
        }

        /*
         * 如果请求来自开发环境, 就不去对比新旧数据及刷新, 直接订阅成功(但是依然需要前面的校验部分)
         * 避免开发环境的代码影响测试环境的listener, 开发环境都是以pull模式本地收消息
         */
        if (request.isDevEnvironment()) {
            log.info("开发环境, 订阅成功");
            return SUCCESS;
        }

        Set<SubscribeMetadata> oldSubscribes = subscribeBizService.listByService(request.getCluster(), request.getService());
        Set<SubscribeMetadata> newSubscribes = toSubscribes(request);

        if (CollectionUtils.isEqualCollection(oldSubscribes, newSubscribes)) {
            log.info("订阅成功, 数据无变化");
            return SUCCESS;
        }

        // 先更新数据库
        subscribeBizService.updateSubscribes(SubscribeContext.builder()
                .cluster(request.getCluster())
                .service(request.getService())
                .env(request.getEnv())
                .build(), newSubscribes);

        // 再通知courier-consumer订阅关系更新了, 发送刷新消息即可
        refresher.refresh();

        log(request, oldSubscribes, newSubscribes);

        return SUCCESS;
    }

    public SubscribeResult unsubscribe(String service) {
        SubscribeManageDTO subscribeManage = subscribeManageMapper.queryByService(service);

        int opCount;
        if (subscribeManage != null) {
            opCount = subscribeManageMapper.update(subscribeManage.setEnable(false));
        } else {
            opCount = subscribeManageMapper.insert(new SubscribeManageDTO()
                    .setService(service)
                    .setEnable(false));
        }

        if (opCount > 0) {
            // 通知courier-consumer订阅关系更新了, 发送刷新消息
            refresher.refresh();
            return SubscribeResult.success("下线成功");
        }
        return SubscribeResult.error("更新表失败");
    }

    /**
     * 校验
     */
    private SubscribeResult validateAndWrap(SubscribeRequest request) {

        if (request == null) {
            return error("订阅请求不能为空");
        }
        if (StringUtils.isBlank(request.getService())) {
            return error("订阅服务service不能为空");
        }

        if (CollectionUtils.isEmpty(request.getTopicGroups())) {
            return null;
        }

        Map<String, String> topicGroupId = Maps.newHashMap();
        Map<String, String> typeTopic = Maps.newHashMap();
        Set<String> wrappedGroupIds = Sets.newHashSet();

        for (TopicGroup topicGroup : request.getTopicGroups()) {

            String topic = topicGroup.getTopic();
            String groupId = topicGroup.getGroupId();

            if (StringUtils.isBlank(topic)) {
                return error("订阅Topic不能为空");
            }
            if (StringUtils.isBlank(groupId)) {
                return error("订阅GroupId不能为空");
            }

            // wrap前缀, 因为后面需要和数据库比对, 虽然直接修改参数不够优雅, 但最简单
            topicGroup.setTopic(Wrapper.wrapTopic(topic));
            topicGroup.setGroupId(Wrapper.wrapGroupId(groupId));

            String existedGroupId = topicGroupId.put(topic, groupId);
            if (existedGroupId != null) {
                return error(format("同一个服务内, 不能使用多个GroupId订阅1个Topic, 发现GroupId:[{0}、{1}]同时订阅Topic:[{2}]", groupId, existedGroupId, topic));
            }

            wrappedGroupIds.add(topicGroup.getGroupId());

            SubscribeResult typesError = validateTypes(topicGroup.getTypes(), typeTopic, topic);
            if (typesError != null) {
                return typesError;
            }
        }

        return validateConflict(request.getCluster(), request.getService(), wrappedGroupIds);
    }

    /**
     * 校验types
     */
    private SubscribeResult validateTypes(Set<String> types, Map<String, String> typeTopic, String topic) {

        if (CollectionUtils.isEmpty(types)) {
            return error("订阅types不能为空");
        }

        for (String type : types) {
            if (StringUtils.isBlank(type)) {
                return error("订阅消息类型不能为空");
            }
            String existedTopic = typeTopic.put(type, topic);
            if (existedTopic != null) {
                return error(format("同一个服务内, 1个消息只能定义在1个Topic内, 发现消息:[{0}]同时定义在Topic:[{1}、{2}]内", type, topic, existedTopic));
            }
        }
        return null;
    }

    /**
     * 校验是否和其他服务的订阅关系冲突
     */
    private SubscribeResult validateConflict(String cluster, String service, Set<String> wrappedGroupIds) {

        SubscribeMetadata conflict = subscribeBizService.checkConflict(cluster, service, wrappedGroupIds);

        if (log.isDebugEnabled()) {
            log.debug("conflict: {}", conflict);
        }

        if (conflict != null) {
            String reason = format("GroupId:[{0}]已被[{1}]-[{2}]使用, 请修改 GroupId。",
                    Wrapper.unWrapGroupId(conflict.getGroupId()), conflict.getCluster(), conflict.getService());
            return error(reason);
        }
        return null;
    }

    /**
     * SubscribeRequest 转 SubscribeMetadata
     */
    private static Set<SubscribeMetadata> toSubscribes(SubscribeRequest request) {

        String cluster = request.getCluster();
        String service = request.getService();

        return CollectionUtils.isEmpty(request.getTopicGroups())
                ? Collections.emptySet()
                : request.getTopicGroups()
                .stream()
                .flatMap(tp -> {
                    String topic = tp.getTopic();
                    String groupId = tp.getGroupId();
                    return tp.getTypes()
                            .stream()
                            .map(t -> new SubscribeMetadata()
                                    .setCluster(cluster)
                                    // env 不作比较
                                    .setService(service)
                                    .setTopic(topic)
                                    .setGroupId(groupId)
                                    // url 不作比较
                                    .setType(t));
                })
                .collect(Collectors.toSet());
    }

    /**
     * 打印详细日志
     */
    private static void log(SubscribeRequest request, Set<SubscribeMetadata> olds, Set<SubscribeMetadata> news) {

        Collection<SubscribeMetadata> delete = CollectionUtils.subtract(olds, news);
        Collection<SubscribeMetadata> add = CollectionUtils.subtract(news, olds);
        Collection<SubscribeMetadata> common = CollectionUtils.intersection(olds, news);

        StringBuilder l = new StringBuilder(512);

        l.append("old:\n");
        if (CollectionUtils.isEmpty(common) && CollectionUtils.isEmpty(delete)) {
            l.append("\t[]\n");
        } else {
            common.forEach(m -> append(l, m, "   "));
            delete.forEach(m -> append(l, m, "[x]"));
        }

        l.append("new:\n");
        if (CollectionUtils.isEmpty(common) && CollectionUtils.isEmpty(add)) {
            l.append("\t[]\n");
        } else {
            common.forEach(m -> append(l, m, "   "));
            add.forEach(m -> append(l, m, "[√]"));
        }

        log.info("订阅成功, 已发送刷新消息, servcie: {}\n{}",
                request.getService(), l);
    }

    private static void append(StringBuilder l, SubscribeMetadata m, String head) {
        l.append("\t").append(head)
                .append(" (topic: ").append(m.getTopic())
                .append(", groupId: ").append(m.getGroupId())
                .append(", type: ").append(m.getType()).append(")")
                .append("\n");
    }

}
