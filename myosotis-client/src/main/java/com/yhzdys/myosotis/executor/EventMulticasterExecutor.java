package com.yhzdys.myosotis.executor;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class EventMulticasterExecutor extends ThreadPoolExecutor {

    public EventMulticasterExecutor() {
        super(0,
                Runtime.getRuntime().availableProcessors() * 2,
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(false),
                new EventMulticasterThreadFactory());
    }

    public static final class EventMulticasterThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("myosotis-multicaster");
            thread.setDaemon(true);
            return thread;
        }
    }
}
