package com.github.open.courier.consumer.service.support;

import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.transport.ConsumeMessage;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author yanglulu
 * @date 2021/8/11
 */
@Data
@Accessors(chain = true)
public class PushContext {

    /**
     * ip port
     */
    private String ipAndPort;

    /**
     * 消息
     */
    private List<ConsumeMessage> messages;

    /**
     * 推送的服务
     */
    private String service;

    /**
     * 重试推送次数
     */
    private Integer retries = 0;

    /**
     * 重试推送最大次数
     */
    private Integer maxTimes = 3;

    /**
     * 数据库 subscribe 表中存的 url
     */
    private String url;

    public void retriesPlus() {
        retries++;
    }

    public boolean canRepush() {
        return StringUtils.endsWith(url, URLConstant.CLIENT_RECEIVES) && retries < maxTimes;
    }

}
