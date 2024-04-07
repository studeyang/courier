package com.github.open.courier.management.application.jobhandler;

import com.github.open.courier.management.application.service.ShardingTableAppService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@JobHandler("DropShardingTableHandler")
public class DropShardingTableHandler extends IJobHandler {

    @Autowired
    private ShardingTableAppService shardingTableAppService;

    @Override
    public ReturnT<String> execute(String param) {

        shardingTableAppService.dropShardingTable();

        return ReturnT.SUCCESS;
    }
}
