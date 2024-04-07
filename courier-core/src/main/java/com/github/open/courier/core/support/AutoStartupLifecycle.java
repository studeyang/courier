package com.github.open.courier.core.support;

import org.springframework.context.SmartLifecycle;

/**
 * Spring中所有bean加载完后, 自动启动的钩子
 */
public abstract class AutoStartupLifecycle implements SmartLifecycle {

    private volatile boolean isRunning = false;

    @Override
    public final boolean isAutoStartup() {
        return true;
    }

    @Override
    public final void start() {
        onStart();
        isRunning = true;
    }

    @Override
    public final void stop(Runnable runnable) {
        stop();
        runnable.run();
    }

    @Override
    public final void stop() {
        onStop();
        isRunning = false;
    }

    @Override
    public final boolean isRunning() {
        return isRunning;
    }

    // -------------------------- 以下方法可以让子类Override ------------------------------

    @Override
    public int getPhase() {
        return 0;
    }

    /**
     * 开始
     */
    public void onStart() {
    }

    /**
     * 结束
     */
    public void onStop() {
    }
}
