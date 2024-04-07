package com.github.open.courier.core.message.subscribe;

import com.github.open.courier.core.message.Subscribe;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 暂停推送
 *
 * @author yanglulu
 */
@Data
@Accessors(chain = true)
public class PausePushSubscribe extends Subscribe {

    /**
     * ip 地址
     */
    private String ip;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * 服务名
     */
    private String service;

}
