package com.github.open.courier.management.service.support;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.naming.NamingService;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
public class NamingServiceHolder {

    @Getter
    @NacosInjected
    private NamingService namingService;

}
