package com.yhzdys.myosotis.event.multicast;

import java.util.Objects;

public final class EventExecutor {

    private final String id;
    private final Runnable runner;

    public EventExecutor(Runnable runner) {
        this.id = null;
        this.runner = runner;
    }

    public EventExecutor(String id, Runnable runner) {
        this.id = id;
        this.runner = runner;
    }

    public void execute() {
        runner.run();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        EventExecutor that = (EventExecutor) object;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
