package com.yhzdys.myosotis.event.multicast.actuator;

import java.util.Objects;

/**
 * command wrapper of myosotis event multicast
 */
public final class EventCommand {

    private final String id;
    private Runnable command;

    /**
     * @param command command
     */
    public EventCommand(Runnable command) {
        this.id = null;
        this.command = command;
    }

    /**
     * @param id      command id
     * @param command command
     */
    public EventCommand(String id, Runnable command) {
        this.id = id;
        this.command = command;
    }

    public String getId() {
        return id;
    }

    public Runnable getCommand() {
        return command;
    }

    public void setCommand(Runnable command) {
        this.command = command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EventCommand that = (EventCommand) o;
        return Objects.equals(id, that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
