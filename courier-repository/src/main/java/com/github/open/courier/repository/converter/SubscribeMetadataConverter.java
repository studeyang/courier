package com.github.open.courier.repository.converter;

import com.github.open.courier.core.transport.SubscribeMetadata;
import com.github.open.courier.repository.entity.SubscribeEntity;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/5/31
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SubscribeMetadataConverter {

    public static SubscribeMetadata converter(SubscribeEntity entity) {
        return new SubscribeMetadata()
                .setCluster(entity.getCluster())
                .setService(entity.getService())
                .setTopic(entity.getTopic())
                .setGroupId(entity.getGroupId())
                .setType(entity.getType())
                .setEnv(entity.getEnvTag() == null ? "" : entity.getEnvTag());
    }

    public static SubscribeEntity convertToSubscribeEntity(SubscribeMetadata subscribeMetadata) {
        return new SubscribeEntity()
                .setCluster(subscribeMetadata.getCluster())
                .setService(subscribeMetadata.getService())
                .setGroupId(subscribeMetadata.getGroupId())
                .setTopic(subscribeMetadata.getTopic())
                .setType(subscribeMetadata.getType());
    }

}
