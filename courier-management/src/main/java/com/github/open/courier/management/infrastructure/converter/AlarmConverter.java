package com.github.open.courier.management.infrastructure.converter;

import com.github.open.courier.core.entity.AlarmEntity;
import com.github.open.courier.core.transport.AlarmDTO;
import com.github.open.courier.core.transport.UpdateAlarmRequest;
import com.github.open.courier.core.transport.AddAlarmRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author yanglulu
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AlarmConverter {

    public static AlarmDTO createDTO(AlarmEntity entity) {

        if (entity == null) {
            return null;
        }

        return new AlarmDTO()
                .setId(entity.getId())
                .setServiceEn(entity.getServiceEn())
                .setServiceCh(entity.getServiceCh())
                .setOwner(entity.getOwner())
                .setMobile(entity.getMobile())
                .setGroupName(entity.getGroupName())
                .setEnabled(entity.getEnabled());
    }

    public static AlarmEntity createEntity(AddAlarmRequest request) {

        if (request == null) {
            return null;
        }

        return new AlarmEntity()
                .setServiceEn(request.getServiceEn())
                .setServiceCh(request.getServiceCh())
                .setOwner(request.getOwner())
                .setMobile(request.getMobile())
                .setGroupName(request.getGroupName())
                .setEnabled(true);
    }

    public static AlarmEntity createEntity(UpdateAlarmRequest request) {

        if (request == null) {
            return null;
        }

        return new AlarmEntity()
                .setId(request.getId())
                .setServiceEn(request.getServiceEn())
                .setServiceCh(request.getServiceCh())
                .setOwner(request.getOwner())
                .setMobile(request.getMobile())
                .setGroupName(request.getGroupName());
    }

}
