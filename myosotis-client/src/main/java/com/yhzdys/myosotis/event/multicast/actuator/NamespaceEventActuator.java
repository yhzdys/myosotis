package com.yhzdys.myosotis.event.multicast.actuator;

import com.yhzdys.myosotis.executor.EventMulticasterExecutor;
import com.yhzdys.myosotis.misc.LoggerFactory;

import java.util.LinkedList;

/**
 * actuator of namespaceListener to handle myosotis config change event
 *
 * @see com.yhzdys.myosotis.entity.MyosotisEvent
 * @see com.yhzdys.myosotis.event.multicast.EventMulticaster
 */
public final class NamespaceEventActuator implements Actuator {
    private final EventMulticasterExecutor executor;
    private final Runnable runner;
    private final LinkedList<EventCommand> commands = new LinkedList<>();
    private boolean running;

    /**
     * @param executor multicasterExecutor
     */
    public NamespaceEventActuator(EventMulticasterExecutor executor) {
        this.executor = executor;
        this.runner = () -> {
            for (; ; ) {
                final EventCommand command;
                synchronized (commands) {
                    command = commands.poll();
                    if (command == null) {
                        running = false;
                        return;
                    }
                }
                try {
                    command.getCommand().run();
                } catch (Throwable t) {
                    LoggerFactory.getLogger().error(t.getMessage(), t);
                }
            }
        };
    }

    @Override
    public void actuate(EventCommand command) {
        synchronized (commands) {
            int index = commands.indexOf(command);
            if (index < 0) {
                commands.add(command);
            } else {
                commands.get(index).setCommand(command.getCommand());
            }
            if (!running) {
                running = true;
                executor.execute(runner);
            }
        }
    }
}