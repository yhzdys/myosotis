package com.yhzdys.myosotis.executor;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * shared thread pool of myosotis event publish
 */
public final class EventPublishExecutor extends ThreadPoolExecutor {

    public EventPublishExecutor() {
        super(2,
                Runtime.getRuntime().availableProcessors(),
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(false),
                new EventPublishThreadFactory());
    }

    public static final class EventPublishThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("myosotis-event-publish");
            thread.setDaemon(true);
            return thread;
        }
    }
}
