package net.azisaba.azisabaachievements.spigot.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AsyncEvent extends Event {
    private static final AtomicLong nextId = new AtomicLong(0);

    public AsyncEvent() {
        super(true);
    }

    /**
     * Calls the event asynchronously (using new thread).
     * @param event the event to call
     * @return true if the event is cancelled (if the event is cancellable); false otherwise (event is not cancelled
     *         or the event is not cancellable)
     */
    public static @NotNull CompletableFuture<@NotNull Boolean> call(@NotNull AsyncEvent event) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        new Thread(() -> {
            try {
                Bukkit.getPluginManager().callEvent(event);
            } finally {
                future.complete(event instanceof Cancellable && ((Cancellable) event).isCancelled());
            }
        }, "AzisabaAchievements-Event-" + nextId.getAndIncrement()).start();
        return future;
    }
}
