package com.github.open.courier.repository.biz;

import com.github.open.courier.core.transport.SubscribeMetadata;
import com.github.open.courier.repository.entity.SubscribeEntity;
import com.github.open.courier.repository.mapper.SubscribeMapper;
import com.github.open.courier.repository.biz.bo.SubscribeContext;
import com.github.open.courier.repository.converter.SubscribeMetadataConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/5/31
 */
@Slf4j
@RequiredArgsConstructor
public class SubscribeBizService {

    private final SubscribeMapper subscribeMapper;

    /**
     * 使用的场景：消息订阅 - 比较新老订阅是否相同
     *
     * @param cluster 集群名
     * @param service 服务名
     * @return 订阅数据元
     */
    public Set<SubscribeMetadata> listByService(String cluster, String service) {
        Set<SubscribeEntity> subscribeEntities = subscribeMapper.listByService(cluster, service);

        // 找不到订阅信息，说明该服务首次订阅
        if (CollectionUtils.isEmpty(subscribeEntities)) {
            return Collections.emptySet();
        }

        return subscribeEntities.stream()
                .map(SubscribeMetadataConverter::converter)
                .collect(Collectors.toSet());
    }

    public Set<SubscribeMetadata> listByServices(String cluster, Set<String> services) {
        Set<SubscribeEntity> subscribeEntities = subscribeMapper.listByServices(cluster, services);
        return subscribeEntities.stream()
                .map(SubscribeMetadataConverter::converter)
                .collect(Collectors.toSet());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteSubscribes(String cluster, String service) {
        subscribeMapper.deleteByClusterAndService(cluster, service);
    }

    public SubscribeMetadata checkConflict(String cluster, String service, Set<String> wrappedGroupIds) {
        List<SubscribeEntity> subscribeEntities = subscribeMapper.checkConflict(wrappedGroupIds);

        if (log.isDebugEnabled()) {
            log.debug("check cluster={}, service={}, wrappedGroupIds={}, result={}", cluster, service, wrappedGroupIds, subscribeEntities);
        }

        // 查出无冲突
        if (CollectionUtils.isEmpty(subscribeEntities)) {
            return null;
        }

        // 判断使用该groupId的服务是否是同一集群
        for (SubscribeEntity subscribeEntity : subscribeEntities) {
            if (!cluster.equals(subscribeEntity.getCluster())) {
                // 不同集群的服务使用了同一个 GroupId，返回冲突详情
                return SubscribeMetadataConverter.converter(subscribeEntity);
            }
            // 下面就是同一个集群的服务使用了同一个 GroupId 的情况了
            // 判断是不是同一个服务
            if (!service.equals(subscribeEntity.getService())) {
                return SubscribeMetadataConverter.converter(subscribeEntity);
            }
        }
        return null;
    }

    public List<SubscribeMetadata> listEnableService() {
        List<SubscribeEntity> subscribeEntities = subscribeMapper.listEnableService();
        return subscribeEntities.stream()
                .map(SubscribeMetadataConverter::converter)
                .collect(Collectors.toList());
    }

    public List<SubscribeMetadata> queryByService(String service) {
        List<SubscribeEntity> subscribeEntities = subscribeMapper.queryByService(service);
        Assert.notEmpty(subscribeEntities, String.format("service=%s 找不到订阅信息", service));

        // 上一步做了断言不为空，这里不会走到 orElse
        String cluster = subscribeEntities.stream().findFirst().orElse(new SubscribeEntity()).getCluster();
        Assert.notNull(cluster, String.format("service=%s 找不到集群名称", service));

        return subscribeEntities.stream()
                .map(SubscribeMetadataConverter::converter)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSubscribes(SubscribeContext subscribeContext, Set<SubscribeMetadata> newSubscribes) {
        String cluster = subscribeContext.getCluster();
        String service = subscribeContext.getService();

        // 先查出 env_tag
        Map<String, String> envTagMap = subscribeMapper.selectEnvTag(cluster, service).stream()
                .collect(Collectors.toMap(e -> e.getTopic() + "|" + e.getType(), SubscribeEntity::getEnvTag));

        deleteSubscribes(cluster, service);
        // 订阅0个消息，就不需要插入数据了
        if (!CollectionUtils.isEmpty(newSubscribes)) {
            // 插入新订阅数据的时候，带上envTag
            insertBatch(envTagMap, newSubscribes);
        }
    }

    /**
     * @param envTagMap envtag数据，例如： topic|com.xxx.UserCreated -> kunlun
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertBatch(Map<String, String> envTagMap,
                            Set<SubscribeMetadata> newSubscribes) {
        Set<SubscribeEntity> subscribeEntities = newSubscribes.stream()
                .map(SubscribeMetadataConverter::convertToSubscribeEntity)
                .collect(Collectors.toSet());

        if (!CollectionUtils.isEmpty(envTagMap)) {
            subscribeEntities.forEach(each -> {
                String envTag = envTagMap.get(each.getTopic() + "|" + each.getType());
                each.setEnvTag(envTag);
            });
        }

        subscribeMapper.insertBatch(subscribeEntities);
    }

}
