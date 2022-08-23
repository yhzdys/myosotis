package com.yhzdys.myosotis.executor;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class MulticasterExecutor extends ThreadPoolExecutor {

    public MulticasterExecutor() {
        super(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<>(true), new EventMulticasterThreadFactory());
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
