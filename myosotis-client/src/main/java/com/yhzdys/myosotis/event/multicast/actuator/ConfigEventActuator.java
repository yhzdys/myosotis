package com.yhzdys.myosotis.event.multicast.actuator;

import com.yhzdys.myosotis.executor.MulticasterExecutor;
import com.yhzdys.myosotis.misc.LoggerFactory;

public final class ConfigEventActuator implements Actuator {

    private final MulticasterExecutor executor;
    private final Runnable runner;
    private boolean running;
    private EventCommand command;

    public ConfigEventActuator(MulticasterExecutor executor) {
        this.executor = executor;
        this.runner = () -> {
            for (; ; ) {
                EventCommand command;
                synchronized (this) {
                    if (this.command == null) {
                        running = false;
                        return;
                    }
                    command = this.command;
                    this.command = null;
                }
                try {
                    command.getRunner().run();
                } catch (Throwable t) {
                    LoggerFactory.getLogger().error(t.getMessage(), t);
                }
            }
        };
    }

    public void actuate(EventCommand command) {
        synchronized (this) {
            this.command = command;
            if (running) {
                return;
            }
            running = true;
            executor.execute(runner);
        }
    }
}
