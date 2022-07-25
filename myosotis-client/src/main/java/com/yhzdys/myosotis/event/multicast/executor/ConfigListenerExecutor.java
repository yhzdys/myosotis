package com.yhzdys.myosotis.event.multicast.executor;

import com.yhzdys.myosotis.misc.LoggerFactory;

import java.util.concurrent.Executor;

/**
 * executor of configListener to handle myosotis config change event
 *
 * @see com.yhzdys.myosotis.entity.MyosotisEvent
 * @see com.yhzdys.myosotis.event.multicast.MyosotisEventMulticaster
 */
public final class ConfigListenerExecutor implements ListenerExecutor {

    private final Object lock = new Object();

    private final Executor sharedPool;
    private final Runnable runner;

    private Runnable task;
    private boolean running;

    public ConfigListenerExecutor(Executor sharedPool) {
        this.sharedPool = sharedPool;
        this.runner = () -> {
            for (; ; ) {
                Runnable currentTask;
                synchronized (lock) {
                    currentTask = task;
                    if (currentTask == null) {
                        running = false;
                        return;
                    }
                    task = null;
                }
                try {
                    currentTask.run();
                } catch (Throwable t) {
                    LoggerFactory.getLogger().error(t.getMessage(), t);
                }
            }
        };
    }

    /**
     * new event will overwrites old event
     */
    public void execute(EventCommand command) {
        synchronized (lock) {
            task = command.getCommand();
            if (running) {
                return;
            }
            running = true;
            sharedPool.execute(runner);
        }
    }
}
