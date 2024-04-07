package com.github.open.courier.core.transport;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 订阅管理数据库映射实体
 *
 * @author LIJIAHAO
 */
@Data
@Accessors(chain = true)
public class SubscribeManageDTO {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 服务名
     */
    private String service;

    /**
     * 指定消费消费节点
     */
    private String consumerNode;

    /**
     * 是否启用
     */
    private boolean enable;
}
