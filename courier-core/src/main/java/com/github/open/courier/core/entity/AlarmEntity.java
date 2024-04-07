package com.github.open.courier.core.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
public class AlarmEntity {

    private Integer id;

    private String serviceEn;

    private String serviceCh;

    private String owner;

    private String groupName;

    private String mobile;

    private Boolean enabled;

}
