package com.yhzdys.myosotis.executor;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * shared thread pool for multicast myosotis event
 */
public final class EventMulticasterExecutor extends ThreadPoolExecutor {

    public EventMulticasterExecutor() {
        super(2,
                Runtime.getRuntime().availableProcessors(),
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(false),
                new InnerThreadFactory());
    }

    /**
     * thread factory of EventMulticasterExecutor
     */
    public static final class InnerThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("myosotis-multicaster");
            thread.setDaemon(true);
            return thread;
        }
    }
}
