package com.yhzdys.myosotis.event.publish.executor;

import com.yhzdys.myosotis.event.publish.MyosotisEventMulticaster;
import com.yhzdys.myosotis.misc.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * executor of configListener to handle myosotis config change event
 *
 * @see com.yhzdys.myosotis.entity.MyosotisEvent
 * @see MyosotisEventMulticaster
 */
public final class ConfigListenerExecutorFactory {

    private final ConcurrentMap<Integer, InnerExecutor> executorMap = new ConcurrentHashMap<>(0);

    private final ThreadPoolExecutor sharedPool;

    public ConfigListenerExecutorFactory(ThreadPoolExecutor sharedPool) {
        this.sharedPool = sharedPool;
    }

    /**
     * get singleton executor of configListenerId
     */
    public Executor getExecutor(Integer listenerId) {
        return executorMap.computeIfAbsent(
                listenerId, id -> new InnerExecutor(this.sharedPool)
        );
    }

    /**
     * new event overwrites old event
     */
    private static final class InnerExecutor implements Executor {

        private final Object lock = new Object();

        private final Executor executor;

        private final Runnable runner;

        private Runnable task;

        private boolean running;

        public InnerExecutor(Executor executor) {
            this.executor = executor;
            this.runner = () -> {
                for (; ; ) {
                    Runnable currentTask;
                    synchronized (this.lock) {
                        currentTask = this.task;
                        if (currentTask == null) {
                            this.running = false;
                            return;
                        }
                        this.task = null;
                    }
                    try {
                        currentTask.run();
                    } catch (Throwable t) {
                        LoggerFactory.getLogger().error(t.getMessage(), t);
                    }
                }
            };
        }

        public void execute(Runnable command) {
            synchronized (this.lock) {
                this.task = command;
                if (this.running) return;

                this.running = true;
                this.executor.execute(this.runner);
            }
        }
    }

}
