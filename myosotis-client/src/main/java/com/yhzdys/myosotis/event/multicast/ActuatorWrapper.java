package com.yhzdys.myosotis.event.multicast;

import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.event.listener.Listener;
import com.yhzdys.myosotis.event.multicast.actuator.Actuator;
import com.yhzdys.myosotis.misc.LoggerFactory;

public final class ActuatorWrapper {

    private final Listener listener;
    private final Actuator actuator;

    ActuatorWrapper(Listener listener, Actuator actuator) {
        this.listener = listener;
        this.actuator = actuator;
    }

    void actuate(MyosotisEvent event) {
        actuator.actuate(
                new EventExecutor(() -> this.trigger(listener, event))
        );
    }

    void actuate(String id, MyosotisEvent event) {
        actuator.actuate(
                new EventExecutor(id, () -> this.trigger(listener, event))
        );
    }

    private void trigger(Listener listener, MyosotisEvent event) {
        try {
            listener.handle(event);
        } catch (Throwable e) {
            LoggerFactory.getLogger().error("Trigger Listener({}) failed, {}", listener.getClass().getName(), e.getMessage());
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ActuatorWrapper that = (ActuatorWrapper) object;
        return listener == (that.listener);
    }

    @Override
    public int hashCode() {
        return listener.hashCode();
    }
}
