package com.github.open.courier.repository.biz.bo;

import lombok.Builder;
import lombok.Data;

/**
 * 订阅业务上下文
 *
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/6/8
 */
@Data
@Builder
public class SubscribeContext {

    private String cluster;
    private String service;
    private String env;
}
