package com.github.open.courier.management.infrastructure.converter;

import com.github.open.courier.core.transport.SubscribeMetadataDTO;
import com.github.open.courier.repository.entity.SubscribeEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SubscribeConverter {

    public static SubscribeMetadataDTO toSubscribeMetadata(SubscribeEntity entity) {

        if (entity == null) {
            return null;
        }

        return new SubscribeMetadataDTO()
                .setCluster(entity.getCluster())
                .setService(entity.getService())
                .setTopic(entity.getTopic())
                .setGroupId(entity.getGroupId())
                .setType(entity.getType())
                .setEnvTag(entity.getEnvTag());
    }

    public static List<SubscribeMetadataDTO> toSubscribeMetadatas(List<SubscribeEntity> entities) {

        return entities.stream()
                .map(SubscribeConverter::toSubscribeMetadata)
                .collect(Collectors.toList());
    }

}
