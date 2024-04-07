package com.github.open.courier.commons.support;

import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.commons.redis.RedisClient;
import com.github.open.courier.core.constant.MessageConstant;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 服务发现客户端
 *
 * @author Courier
 */
@Slf4j
public class CourierDiscoveryClient implements ServiceDiscovery {

    private final DiscoveryClient discoveryClient;
    private final RedisClient redisClient;
    private final AssignConsume assignConsume;

    public CourierDiscoveryClient(DiscoveryClient discoveryClient,
                                  RedisClient redisClient,
                                  AssignConsume assignConsume) {
        this.discoveryClient = discoveryClient;
        this.redisClient = redisClient;
        this.assignConsume = assignConsume;
    }

    /**
     * 选择一个节点
     *
     * @param service 服务名
     * @return ip port
     */
    public String choose(String service) {

        List<String> list = Lists.newArrayList();

        // 可以不依赖 Redis
        if (redisClient != null) {
            List<String> pausedList = redisClient.listAll(String.format(MessageConstant.PAUSE_LIST, service));
            List<String> stoppedList = redisClient.listAll(String.format(MessageConstant.STOPPED_LIST, service));

            list.addAll(pausedList);
            list.addAll(stoppedList);
        }

        return choose(service, list);
    }

    /**
     * 选择一个节点
     *
     * @param service 服务名
     * @param exclude 排除的节点
     * @return ip port
     */
    public String choose(String service, List<String> exclude) {

        List<String> urlList = getServiceHostAndPort(service);

        if (CollectionUtils.isEmpty(urlList)) {
            log.warn("未找到服务节点, service: [{}]", service);
        }

        List<String> pausedList = redisClient.listAll(String.format(MessageConstant.PAUSE_LIST, service));
        List<String> stoppedList = redisClient.listAll(String.format(MessageConstant.STOPPED_LIST, service));

        List<String> list = new ArrayList<>();
        list.addAll(pausedList);
        list.addAll(stoppedList);
        list.addAll(exclude);

        if (ListUtils.subtract(urlList, list).isEmpty()) {
            return assignConsume.assignIfNecessary(urlList, service);
        }

        return assignConsume.assignIfNecessary(ListUtils.subtract(urlList, list), service);
    }

    @Override
    public List<String> getServiceHostAndPort(String service) {

        List<ServiceInstance> instances = discoveryClient.getInstances(service);

        if (CollectionUtils.isEmpty(instances)) {
            return Collections.emptyList();
        }

        return toIpAndPortList(instances);
    }

    private List<String> toIpAndPortList(List<ServiceInstance> instances) {
        return instances
                .stream()
                .map(this::concatHostAndPort)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
    }

    /**
     * 获取服务url集合
     *
     * @param service 服务名
     * @return url集合
     */
    public List<String> getServiceUrl(String service) {

        return getServiceHostAndPort(service).stream()
                .map(URLConstant.HTTP_PREFIX::concat)
                .collect(Collectors.toList());
    }

    /**
     * 获取服务接收消息url
     *
     * @param service 服务名
     */
    public List<String> getServiceUrlWithMessagesReceive(String service) {

        return getServiceHostAndPort(service).stream()
                .map(URLConstant.HTTP_PREFIX::concat)
                .map(s -> s.concat(URLConstant.CLIENT_RECEIVES))
                .collect(Collectors.toList());
    }

    /**
     * 拼接实例的 host和port
     */
    private String concatHostAndPort(ServiceInstance instance) {

        if (StringUtils.isBlank(instance.getHost())) {
            return StringUtils.EMPTY;
        }

        return instance.getHost() +
                ':' +
                instance.getPort();
    }

}
