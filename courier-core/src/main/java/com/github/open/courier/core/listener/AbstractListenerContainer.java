package com.github.open.courier.core.listener;

import com.github.open.courier.core.support.AutoStartupLifecycle;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Listener容器
 *
 * @author Courier
 */
@Getter
public abstract class AbstractListenerContainer extends AutoStartupLifecycle {

    private final Map<String, AbstractConsumerListener> listenerMap = new ConcurrentHashMap<>();
    private final ExecutorService listenerPool = Executors.newCachedThreadPool();

    /**
     * 启动时, 加载并启动listener
     */
    @Override
    public void onStart() {
        init().forEach(this::start);
    }

    /**
     * 停止时, 停止所有listener
     */
    @Override
    public void onStop() {
        listenerMap.values().forEach(AbstractConsumerListener::stop);
        listenerPool.shutdown();
    }

    /**
     * 启动listener
     */
    public final void start(AbstractConsumerListener listener) {
        listenerMap.put(listener.getName(), listener);
        listenerPool.execute(listener);
    }

    /**
     * 停止listener
     */
    public final void stop(String listenerName) {
        listenerMap.remove(listenerName).stop();
    }

    /**
     * 加载listener
     */
    public abstract List<AbstractConsumerListener> init();

    /**
     * 刷新容器
     */
    public abstract void refresh();

}
