package com.github.open.courier.core.transport;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author yanglulu
 */
@Data
@Accessors(chain = true)
public class AddAlarmRequest {

    private String serviceEn;

    private String serviceCh;

    private String owner;

    private String groupName;

    private String mobile;

    private Boolean enabled;

}
