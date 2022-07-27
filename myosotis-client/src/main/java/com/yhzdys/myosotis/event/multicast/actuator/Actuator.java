package com.yhzdys.myosotis.event.multicast.actuator;

/**
 * actuator of listener for multicast event
 */
public interface Actuator {

    /**
     * actuate the given command at some time in the future.
     */
    void actuate(EventCommand command);
}
