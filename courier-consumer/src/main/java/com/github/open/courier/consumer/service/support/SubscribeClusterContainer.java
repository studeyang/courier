package com.github.open.courier.consumer.service.support;

import com.github.open.courier.repository.entity.SubscribeEntity;
import com.github.open.courier.repository.mapper.SubscribeMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/6/9
 */
@Component
public class SubscribeClusterContainer {

    /**
     * 例如：{"cassmall", "kunlun"} -> http://mall-agent-kunlun
     */
    private Map<ClusterAndEnv, String> clusterEnv2UrlMap;

    /**
     * 例如：CID_alikafka_ALPHA_EXAMPLE -> cassmall
     */
    private Map<String, String> groupId2ClusterMap;

    /**
     * 例如：{"CID_alikafka_ALPHA_EXAMPLE", "com.xxx.courier.example.event.ExampleEvent"} -> kunlun
     */
    private Map<GroupIdAndType, String> groupIdType2EnvTagMap;

    private final SubscribeMapper subscribeMapper;

    public SubscribeClusterContainer(SubscribeMapper subscribeMapper) {
        this.subscribeMapper = subscribeMapper;

        refresh();
    }

    public String selectUrl(String toCluster, String toEnv) {
        return clusterEnv2UrlMap.get(new ClusterAndEnv(toCluster, toEnv));
    }

    public String whereCluster(String groupId) {
        return groupId2ClusterMap.get(groupId);
    }

    public boolean haveBeenAssign(String groupId, String type) {
        return groupIdType2EnvTagMap.containsKey(new GroupIdAndType(groupId, type));
    }

    public String getEnvTag(String groupId, String type) {
        return groupIdType2EnvTagMap.get(new GroupIdAndType(groupId, type));
    }

    public void refresh() {
        this.groupId2ClusterMap = getGroupId2ClusterMap();
        this.groupIdType2EnvTagMap = getGroupIdType2EnvTagMap();
    }

    private Map<String, String> getGroupId2ClusterMap() {
        return subscribeMapper.listEnableService().stream()
                .collect(Collectors.toMap(SubscribeEntity::getGroupId, SubscribeEntity::getCluster, (k, v) -> v));
    }

    private Map<GroupIdAndType, String> getGroupIdType2EnvTagMap() {
        return subscribeMapper.findAssignedGroupIdAndType().stream()
                .collect(
                        Collectors.toMap(
                                subscribe -> new GroupIdAndType(subscribe.getGroupId(), subscribe.getType()),
                                SubscribeEntity::getEnvTag)
                );
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode
    private static class ClusterAndEnv {
        private String cluster;
        private String env;
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode
    private static class GroupIdAndType {
        private String groupId;
        private String type;
    }

}
