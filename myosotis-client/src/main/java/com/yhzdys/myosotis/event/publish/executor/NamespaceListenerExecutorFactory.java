package com.yhzdys.myosotis.event.publish.executor;

import com.yhzdys.myosotis.event.publish.MyosotisEventMulticaster;
import com.yhzdys.myosotis.misc.LoggerFactory;

import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * executor of namespaceListener to handle myosotis config change event
 *
 * @see com.yhzdys.myosotis.entity.MyosotisEvent
 * @see MyosotisEventMulticaster
 */
public final class NamespaceListenerExecutorFactory {

    private final LinkedList<Task> tasks = new LinkedList<>();

    private final ThreadPoolExecutor sharedPool;

    private final Runnable runner;

    private boolean running;

    public NamespaceListenerExecutorFactory(ThreadPoolExecutor sharedPool) {
        this.sharedPool = sharedPool;
        this.runner = () -> {
            for (; ; ) {
                final Task task;
                synchronized (tasks) {
                    task = tasks.poll();
                    if (task == null) {
                        running = false;
                        return;
                    }
                }
                try {
                    task.getCommand().run();
                } catch (Throwable t) {
                    LoggerFactory.getLogger().error(t.getMessage(), t);
                }
            }
        };
    }

    public void execute(Task task) {
        synchronized (tasks) {
            int index = tasks.indexOf(task);
            if (index == -1) {
                tasks.add(task);
            } else {
                tasks.get(index).setCommand(task.getCommand());
            }
            if (!running) {
                running = true;
                sharedPool.execute(runner);
            }
        }
    }

    public static final class Task {

        private final String key;

        private Runnable command;

        public Task(String key, Runnable command) {
            this.key = key;
            this.command = command;
        }

        public String getKey() {
            return key;
        }

        public Runnable getCommand() {
            return command;
        }

        public void setCommand(Runnable command) {
            this.command = command;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Task task = (Task) o;
            return key.equals(task.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }
    }
}