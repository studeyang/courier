package com.github.open.courier.producer.restful;

import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.producer.application.RefreshAppService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用于刷新consumer和management中的所有listener
 * 正常情况是用不到这个接口的, 怕线上listener与数据库不同步(理论上不可能), 专门开了这个接口用于刷新全部listener
 */
@Api(tags = "刷新Listener容器服务")
@Slf4j
@RestController
@RequiredArgsConstructor
public class RefreshService {

    private final RefreshAppService refresher;

    @ApiOperation("刷新所有listener")
    @PostMapping(URLConstant.PRODUCER_REFRESH)
    public void refreshAllListener() {
        refresher.refresh();
    }

}
