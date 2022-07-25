package com.yhzdys.myosotis.event.multicast.executor;

/**
 * executor of listener for multicast event
 */
public interface ListenerExecutor {

    /**
     * Executes the given command at some time in the future.
     */
    void execute(EventCommand command);
}
