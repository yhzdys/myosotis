package com.yhzdys.myosotis.event.multicast.actuator;

import com.yhzdys.myosotis.executor.EventMulticasterExecutor;
import com.yhzdys.myosotis.misc.LoggerFactory;

public final class ConfigEventActuator implements Actuator {

    private final EventMulticasterExecutor executor;
    private final Runnable runner;
    private boolean running;
    private EventCommand command;

    public ConfigEventActuator(EventMulticasterExecutor executor) {
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
                    command.getCommand().run();
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
