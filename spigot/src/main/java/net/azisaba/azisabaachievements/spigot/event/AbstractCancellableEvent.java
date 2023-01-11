package net.azisaba.azisabaachievements.spigot.event;

import org.bukkit.event.Cancellable;

public abstract class AbstractCancellableEvent extends AbstractEvent implements Cancellable {
    private boolean cancel = false;

    public AbstractCancellableEvent() {
        super();
    }

    public AbstractCancellableEvent(boolean isAsync) {
        super(isAsync);
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }
}
