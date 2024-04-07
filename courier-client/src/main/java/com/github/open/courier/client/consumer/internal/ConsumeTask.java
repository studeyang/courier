package com.github.open.courier.client.consumer.internal;

import com.github.open.courier.core.converter.MessageJsonConverter;
import com.github.open.courier.core.transport.ConsumeMessage;
import com.github.open.courier.messaging.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * 消费task
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class ConsumeTask implements Runnable {

    final ConsumeSupport consumeSupport;
    final ConsumeMessage consumeMessage;
    final Date receiveStamp;

    Message message;
    MessageHandler handler;
    long beforeConsumeStamp;
    long afterConsumeStamp;

    @Override
    public void run() {

        ConsumeMessage cm = this.consumeMessage;

        this.message = MessageJsonConverter.toObject(cm.getContent(), Message.class);
        if (message == null) {
            consumeSupport.fail(cm, "kafka消息json转换为Message对象失败", false);
            return;
        }

        this.handler = consumeSupport.getHandler(message);
        if (handler == null) {
            consumeSupport.fail(cm, "kafka消费找不到handler", false);
            return;
        }

        log.info("kafka开始消费消息, cid:{}, message:{}", cm.getId(), cm.getContent());

        Exception fail = null;
        try {
            doConsume();
        } catch (Exception e) {
            log.error("反射调用异常，请检查方法 handler.method:{}, message:{}", handler.toString(), cm.getContent(), e);
            fail = e;
        }
        if (fail == null) {
            consumeSupport.success(this);
        } else {
            consumeSupport.retry(this, fail);
        }
    }

    /**
     * 执行handle方法
     */
    void doConsume() throws Exception {
        this.beforeConsumeStamp = System.currentTimeMillis();
        handler.invoke(message);
        this.afterConsumeStamp = System.currentTimeMillis();
    }

    /**
     * 等待耗时
     */
    long getWaitCost() {
        return beforeConsumeStamp - receiveStamp.getTime();
    }

    /**
     * 执行handle方法耗时
     */
    long getConsumeCost() {
        return afterConsumeStamp - beforeConsumeStamp;
    }

    /**
     * 是否是顺序消息
     */
    public boolean isSequence() {
        return StringUtils.isNotEmpty(consumeMessage.getPrimaryKey());
    }

    /**
     * 是否是重试任务
     */
    public boolean isRetry() {
        return false;
    }
}
