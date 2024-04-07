package com.github.open.courier.admin.client;

import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.transport.*;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(value = "courier-management", url = "${courier.server.management:service-courier-management:11113}")
public interface AdminClient {

    @RequestMapping(value = URLConstant.MANAGEMENT_QUERY_SEND_SUCCESS, method = RequestMethod.POST)
    CommonPageDTO querySendSuccess(@RequestBody QuerySendMessageRequest param);

    @RequestMapping(value = URLConstant.MANAGEMENT_QUERY_SEND_FAIL, method = RequestMethod.POST)
    CommonPageDTO querySendFail(@RequestBody QuerySendMessageRequest param);

    @RequestMapping(value = URLConstant.MANAGEMENT_QUERY_CONSUME_SUCCESS, method = RequestMethod.POST)
    CommonPageDTO queryConsumeSuccess(@RequestBody QueryConsumeMessageRequest request);

    @RequestMapping(value = URLConstant.MANAGEMENT_QUERY_CONSUME_FAIL, method = RequestMethod.POST)
    CommonPageDTO queryConsumeFail(@RequestBody QueryConsumeMessageRequest request);

    @RequestMapping(value = URLConstant.MANAGEMENT_RESEND, method = RequestMethod.POST)
    List<MessageSendResult> resend(@RequestBody QueryOperationRequest operationRequest);

    @RequestMapping(value = URLConstant.MANAGEMENT_RECONSUME, method = RequestMethod.POST)
    List<ReconsumeResult> reconsume(@RequestBody QueryOperationRequest operationRequest);

    @RequestMapping(value = URLConstant.MANAGEMENT_SUBSCRIBE_BIND, method = RequestMethod.POST)
    int bind(@RequestBody SubscribeManageRequest request);

    @RequestMapping(value = URLConstant.MANAGEMENT_QUERY_ALL_SUBSCRIBE_MANAGE, method = RequestMethod.POST)
    List<SubscribePageDTO> queryAllSubscribePageDTO();

    @RequestMapping(value = URLConstant.MANAGEMENT_QUERY_SUBSCRIBE_MANAGE, method = RequestMethod.POST)
    List<SubscribePageDTO> queryOneSubscribePageDTO(@RequestBody String serviceName);

}
