package com.github.open.courier.admin.controller;

import com.github.open.courier.admin.aop.Log;
import com.github.open.courier.admin.client.AdminClient;
import com.github.open.courier.admin.client.ProducerClient;
import com.github.open.courier.core.transport.*;
import io.github.open.toolkit.commons.model.Result;
import io.github.open.toolkit.commons.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class MessageController {

    @Autowired
    private AdminClient adminClient;
    @Autowired
    private ProducerClient producerClient;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/send")
    public String send(@RequestParam(value = "messageId", required = false) String messageId,
                       @RequestParam(value = "consumeTime", required = false, defaultValue = "0") long consumeTime,
                       Model model) {
        model.addAttribute("messageId", messageId);
        model.addAttribute("consumeTime", consumeTime);
        return "message/send-success";
    }

    @GetMapping("/send/fail")
    public String sendFail() {
        return "message/send-fail";
    }

    @GetMapping("/consume")
    public String consume(@RequestParam(value = "messageId", required = false) String messageId,
                          @RequestParam(value = "createAt", required = false, defaultValue = "0") long createAt,
                          Model model) {
        model.addAttribute("messageId", messageId);
        model.addAttribute("createAt", createAt);
        return "message/consume-success";
    }

    @GetMapping("/consume/fail")
    public String consumeFail() {
        return "message/consume-fail";
    }

    @GetMapping("/operations")
    public String operation() {
        return "message/operation";
    }

    @GetMapping("/subscribe/management")
    public ModelAndView serviceManagement(@RequestParam(value = "serviceName", required = false) String serviceName) {

        ModelAndView mv = new ModelAndView("message/subscribe-management");

        if (StringUtil.isEmpty(serviceName)) {
            mv.addObject("subscribes", adminClient.queryAllSubscribePageDTO());
        } else {
            mv.addObject("subscribes", adminClient.queryOneSubscribePageDTO(serviceName));
        }
        mv.addObject("serviceName", StringUtil.isEmpty(serviceName) ? "" : serviceName);
        return mv;
    }

    @PostMapping("/subscribe/bind")
    @ResponseBody
    public Result<Void> bind(SubscribeManageRequest request) {

        try {
            adminClient.bind(request);
            return Result.success();
        } catch (Exception e) {
            log.warn("指定消费节点失败, Service:{}", request.getService(), e);
        }
        return Result.error("指定消费节点失败");

    }

    @PostMapping("/subscribe/unsubscribe")
    @ResponseBody
    public Result<Void> unsubscribe(SubscribeManageRequest request) {

        SubscribeResult unsubscribeResult = producerClient.unsubscribeService(request);

        if (unsubscribeResult.isSuccess()) {

            return Result.success();

        } else {

            log.warn("服务下线失败, Service:{} reason:{}", request.getService(), unsubscribeResult.getReason());

            return Result.error("服务下线失败,原因：" + unsubscribeResult.getReason());
        }
    }

    @Log
    @ResponseBody
    @PostMapping("/pageList/send/success")
    public CommonPageDTO querySendSuccess(QuerySendMessageRequest request) {
        return adminClient.querySendSuccess(request);
    }

    @Log
    @ResponseBody
    @PostMapping("/pageList/send/fail")
    public CommonPageDTO querySendFail(QuerySendMessageRequest request) {
        return adminClient.querySendFail(request);
    }

    @Log
    @ResponseBody
    @PostMapping("/pageList/consume/success")
    public CommonPageDTO queryConsumeSuccess(QueryConsumeMessageRequest request) {
        return adminClient.queryConsumeSuccess(request);
    }

    @Log
    @ResponseBody
    @PostMapping("/pageList/consume/fail")
    public CommonPageDTO queryConsumeFail(QueryConsumeMessageRequest request) {
        return adminClient.queryConsumeFail(request);
    }

    @Log
    @ResponseBody
    @PostMapping("/resend")
    public Result<List<MessageSendResult>> resend(@RequestBody QueryOperationRequest operationRequest) {

        List<String> params = operationRequest.getIds().stream().distinct().collect(Collectors.toList());

        try {
            operationRequest.setIds(params);
            return Result.success(adminClient.resend(operationRequest));
        } catch (Exception e) {
            log.warn("重新发送消息失败, messageIds:{}", operationRequest.getIds(), e);
        }
        return Result.error("重新发送消息失败");
    }

    @Log
    @ResponseBody
    @PostMapping("/reconsume")
    public Result<List<ReconsumeResult>> reconsume(@RequestBody QueryOperationRequest operationRequest) {

        List<String> params = operationRequest.getIds().stream().distinct().collect(Collectors.toList());

        try {
            operationRequest.setIds(params);
            return Result.success(adminClient.reconsume(operationRequest));
        } catch (Exception e) {
            log.warn("重新消费消息失败, ids:{}", operationRequest.getIds(), e);
        }
        return Result.error("重新消费消息失败");
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }
}
