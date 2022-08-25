package com.yhzdys.myosotis.event.multicast.actuator;

import com.yhzdys.myosotis.event.multicast.EventExecutor;

public interface Actuator {

    void actuate(EventExecutor executor);
}
