package com.yhzdys.myosotis.event.multicast.actuator;

import com.yhzdys.myosotis.executor.MulticasterExecutor;
import com.yhzdys.myosotis.misc.LoggerFactory;

import java.util.LinkedList;

public final class NamespaceEventActuator implements Actuator {

    private final MulticasterExecutor executor;
    private final Runnable runner;
    private final LinkedList<EventCommand> commands = new LinkedList<>();
    private boolean running;

    public NamespaceEventActuator(MulticasterExecutor executor) {
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
                    command.getRunner().run();
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
                commands.get(index).setRunner(command.getRunner());
            }
            if (!running) {
                running = true;
                executor.execute(runner);
            }
        }
    }
}