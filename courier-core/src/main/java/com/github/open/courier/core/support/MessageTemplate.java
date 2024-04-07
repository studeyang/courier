package com.github.open.courier.core.support;

import com.github.open.courier.core.transport.ThreadPoolAlarmMetadata;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 消息模板
 *
 * @author yanglulu
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageTemplate {

    public static String messageConsumeFail(String title,
                                            List<String> events,
                                            List<String> messageIds,
                                            String reason) {
        StringBuilder messageBuilder = new StringBuilder();

        messageBuilder.append("\n【").append(title).append("】\n");

        messageBuilder.append("消息类型：").append("\n");
        for (String event : events) {
            messageBuilder.append(event).append("\n");
        }

        messageBuilder.append("messageId：").append("\n");
        for (String messageId : messageIds) {
            messageBuilder.append(messageId).append("\n");
        }

        messageBuilder.append("reason：").append("\n");
        messageBuilder.append(reason);

        return StringUtils.removeEnd(messageBuilder.toString(), "\n");
    }

    public static String handleTooLongText(String title, Map<String, Long> consumeIdToCostTime) {

        StringBuilder messageBuilder = new StringBuilder();

        messageBuilder.append("\n【").append(title).append("】");

        messageBuilder.append("\n(消费id -> cost time)\n");

        consumeIdToCostTime.forEach((consumeId, costTime) -> {
            messageBuilder.append(consumeId);
            messageBuilder.append(" -> ");
            messageBuilder.append(costTime);
            messageBuilder.append(" ms");
            messageBuilder.append("\n");
        });

        return StringUtils.removeEnd(messageBuilder.toString(), "\n");
    }

    public static String handleThreadPoolWarnText(ThreadPoolAlarmMetadata metadata) {

        StringBuilder messageBuilder = new StringBuilder();

        messageBuilder.append("\n【消费线程池告警】").append("\n");

        messageBuilder.append("节点IP：").append(metadata.getNodeIp()).append("\n");
        messageBuilder.append("队列容量：").append(metadata.getQueueCapacity()).append("\n");
        messageBuilder.append("告警阈值：").append(metadata.getPauseThreshold()).append("\n");
        messageBuilder.append("任务数量：").append(metadata.getQueueSize());

        return StringUtils.removeEnd(messageBuilder.toString(), "\n");
    }

    public static String handleThreadPoolRecoverText(ThreadPoolAlarmMetadata metadata) {

        StringBuilder messageBuilder = new StringBuilder();

        messageBuilder.append("\n【消费线程池告警恢复】").append("\n");

        messageBuilder.append("节点IP：").append(metadata.getNodeIp()).append("\n");
        messageBuilder.append("队列容量：").append(metadata.getQueueCapacity()).append("\n");
        messageBuilder.append("恢复阈值：").append(metadata.getResumeThreshold()).append("\n");
        messageBuilder.append("任务数量：").append(metadata.getQueueSize());

        return StringUtils.removeEnd(messageBuilder.toString(), "\n");
    }

}
