package com.github.open.courier.core.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.open.courier.core.listener.ListenerState.*;

/**
 * 消费开关
 *
 * @author yanglulu
 */
@Slf4j
public class ListenerSwitch {

    /**
     * 状态，考虑扩展性
     */
    private final AtomicReference<ListenerState> listenerState;

    /**
     * 防止多次调用 kafka 的 pause() 方法
     */
    private final AtomicBoolean kafkaPaused;

    public ListenerSwitch() {
        this.listenerState = new AtomicReference<>(RUNING);
        this.kafkaPaused = new AtomicBoolean(false);
    }

    /**
     * 设置为暂停状态
     */
    public final void pause() {
        listenerState.set(PAUSE);
    }

    /**
     * 设置为恢复状态
     */
    public final void resume() {
        listenerState.set(RESUME);
    }

    /**
     * 设置为停止状态
     */
    public final void stop() {
        listenerState.set(STOP);
    }

    /**
     * 设置为kafka暂停状态
     */
    public final void setKafkaPaused(Boolean paused) {
        this.kafkaPaused.set(paused);
    }

    /**
     * 获取kafka暂停状态
     *
     * @return kafka暂停状态
     */
    public final boolean getKafkaPaused() {
        return kafkaPaused.get();
    }

    /**
     * 获取listener状态
     * <p>这个状态不应该对外暴露，目前只供 ConsumerMetricsService 接口查看状态使用</p>
     *
     * @return listener状态
     */
    public final ListenerState getListenerState() {
        return listenerState.get();
    }

    /**
     * 已经停止了吗？
     *
     * @return 是/否
     */
    public final boolean isStop() {
        return listenerState.get() == STOP;
    }

    /**
     * 是暂停状态吗？
     *
     * @return 是/否
     */
    public final boolean isPause() {
        return listenerState.get() == PAUSE;
    }

    /**
     * 是恢复状态吗？
     *
     * @return 是/否
     */
    public final boolean isResume() {
        return listenerState.get() == RESUME;
    }

    /**
     * 暂停消费
     *
     * @param consumer     kafka 消费者
     * @param mode         courier 消费模式
     * @param service      业务服务名
     * @param listenerName listenerName
     */
    public final void pauseConsume(Consumer<String, String> consumer, String mode, String service, String listenerName) {

        if (getKafkaPaused()) {
            // 已经是暂停状态，不需要再次暂停
            return;
        }

        setKafkaPaused(true);

        consumer.pause(consumer.assignment());

        log.info("【{}】kafka消费者 {} 已暂停, listener: {}", mode, service, listenerName);
    }

    /**
     * 恢复消费
     *
     * @param consumer     kafka 消费者
     * @param mode         courier 消费模式
     * @param service      业务服务名
     * @param listenerName listenerName
     */
    public final void resumeConsume(Consumer<String, String> consumer, String mode, String service, String listenerName) {

        if (getKafkaPaused()) {
            // 暂停状态才需要恢复
            setKafkaPaused(false);

            consumer.resume(consumer.paused());

            log.info("【{}】kafka消费者 {} 已恢复, listener: {}", mode, service, listenerName);
        }
    }

}