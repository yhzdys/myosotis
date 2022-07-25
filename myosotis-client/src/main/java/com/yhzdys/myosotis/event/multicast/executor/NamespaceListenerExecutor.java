package com.yhzdys.myosotis.event.multicast.executor;

import com.yhzdys.myosotis.misc.LoggerFactory;

import java.util.LinkedList;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * executor of namespaceListener to handle myosotis config change event
 *
 * @see com.yhzdys.myosotis.entity.MyosotisEvent
 * @see com.yhzdys.myosotis.event.multicast.MyosotisEventMulticaster
 */
public final class NamespaceListenerExecutor implements ListenerExecutor {

    private final LinkedList<EventCommand> commands = new LinkedList<>();

    private final ThreadPoolExecutor sharedPool;
    private final Runnable runner;

    private boolean running;

    public NamespaceListenerExecutor(ThreadPoolExecutor sharedPool) {
        this.sharedPool = sharedPool;
        this.runner = () -> {
            for (; ; ) {
                final EventCommand task;
                synchronized (commands) {
                    task = commands.poll();
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

    @Override
    public void execute(EventCommand command) {
        synchronized (commands) {
            int index = commands.indexOf(command);
            if (index == -1) {
                commands.add(command);
            } else {
                commands.get(index).setCommand(command.getCommand());
            }
            if (!running) {
                running = true;
                sharedPool.execute(runner);
            }
        }
    }
}