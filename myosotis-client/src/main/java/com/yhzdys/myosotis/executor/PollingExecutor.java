package com.yhzdys.myosotis.executor;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

public final class PollingExecutor extends ScheduledThreadPoolExecutor {

    public PollingExecutor() {
        super(1, new ScheduleThreadFactory());
    }

    private static final class ScheduleThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("myosotis-polling");
            thread.setDaemon(true);
            return thread;
        }
    }
}
