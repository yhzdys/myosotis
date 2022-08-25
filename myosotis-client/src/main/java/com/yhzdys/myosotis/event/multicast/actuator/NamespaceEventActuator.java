package com.yhzdys.myosotis.event.multicast.actuator;

import com.yhzdys.myosotis.event.multicast.EventExecutor;
import com.yhzdys.myosotis.executor.MulticasterExecutor;
import com.yhzdys.myosotis.misc.LoggerFactory;

import java.util.LinkedList;

public final class NamespaceEventActuator implements Actuator {

    private final MulticasterExecutor mExecutor;
    private final Runnable runner;
    private final LinkedList<EventExecutor> executors = new LinkedList<>();
    private boolean running;

    public NamespaceEventActuator(MulticasterExecutor mExecutor) {
        this.mExecutor = mExecutor;
        this.runner = () -> {
            for (; ; ) {
                EventExecutor currentExecutor;
                synchronized (executors) {
                    currentExecutor = executors.poll();
                    if (currentExecutor == null) {
                        running = false;
                        return;
                    }
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
        synchronized (executors) {
            int index = executors.indexOf(executor);
            if (index < 0) {
                executors.add(executor);
            } else {
                executors.set(index, executor);
            }
            if (!running) {
                running = true;
                this.mExecutor.execute(runner);
            }
        }
    }
}
