package com.yhzdys.myosotis.executor;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * thread pool for myosotis long polling
 */
public final class LongPollingExecutor extends ScheduledThreadPoolExecutor {

    public LongPollingExecutor() {
        super(2, new PollingThreadFactory());
    }

    private static final class PollingThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("myosotis-polling");
            thread.setDaemon(true);
            return thread;
        }
    }
}
