package com.github.open.courier.management.application.jobhandler;

import com.github.open.courier.management.application.service.ConsumerHolderAppService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 定时任务：groupId 存活检测, 定时清理已经不存在的 consumer 节点
 */
@Component
@JobHandler("GroupIdHealthCheckHandler")
public class ConsumerHolderHandler extends IJobHandler {

    @Autowired
    private ConsumerHolderAppService consumerHolderAppService;

    @Override
    public ReturnT<String> execute(String strings) {

        consumerHolderAppService.groupIdSurviveCheck();

        return ReturnT.SUCCESS;
    }
}
