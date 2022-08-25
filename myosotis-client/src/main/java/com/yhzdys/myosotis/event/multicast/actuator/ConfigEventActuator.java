package com.yhzdys.myosotis.event.multicast.actuator;

import com.yhzdys.myosotis.event.multicast.EventExecutor;
import com.yhzdys.myosotis.executor.MulticasterExecutor;
import com.yhzdys.myosotis.misc.LoggerFactory;

public final class ConfigEventActuator implements Actuator {

    private final MulticasterExecutor mExecutor;
    private final Runnable runner;
    private boolean running;
    private EventExecutor executor;

    public ConfigEventActuator(MulticasterExecutor mExecutor) {
        this.mExecutor = mExecutor;
        this.runner = () -> {
            for (; ; ) {
                EventExecutor currentExecutor;
                synchronized (this) {
                    if (executor == null) {
                        running = false;
                        return;
                    }
                    currentExecutor = executor;
                    executor = null;
                }
                try {
                    currentExecutor.execute();
                } catch (Throwable t) {
                    LoggerFactory.getLogger().error(t.getMessage(), t);
                }
            }
        };
    }

    @Override
    public void actuate(EventExecutor executor) {
        synchronized (this) {
            this.executor = executor;
            if (running) {
                return;
            }
            running = true;
            mExecutor.execute(runner);
        }
    }
}
