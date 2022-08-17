package com.yhzdys.myosotis.event.multicast.actuator;

import java.util.Objects;

public final class EventCommand {

    private final String id;
    private Runnable runner;

    public EventCommand(Runnable runner) {
        this.id = null;
        this.runner = runner;
    }

    public EventCommand(String id, Runnable runner) {
        this.id = id;
        this.runner = runner;
    }

    public String getId() {
        return id;
    }

    public Runnable getRunner() {
        return runner;
    }

    public void setRunner(Runnable runner) {
        this.runner = runner;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        EventCommand that = (EventCommand) object;
        return Objects.equals(id, that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
