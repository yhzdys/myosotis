package com.yhzdys.myosotis.executor;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * thread pool for myosotis long polling
 */
public final class ScheduledExecutor extends ScheduledThreadPoolExecutor {

    public ScheduledExecutor() {
        super(2, new InnerThreadFactory());
    }

    private static final class InnerThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("myosotis-schedule");
            thread.setDaemon(true);
            return thread;
        }
    }
}
