package net.azisaba.azisabaachievements.spigot.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class AbstractEvent extends Event {
    public AbstractEvent() {
        super();
    }

    public AbstractEvent(boolean isAsync) {
        super(isAsync);
    }

    /**
     * Calls an event and returns the result.
     * @return true if cancelled, if cancellable. Otherwise, false.
     */
    public boolean callEvent() {
        Bukkit.getPluginManager().callEvent(this);
        if (this instanceof Cancellable) {
            return ((Cancellable) this).isCancelled();
        } else {
            return false;
        }
    }
}
