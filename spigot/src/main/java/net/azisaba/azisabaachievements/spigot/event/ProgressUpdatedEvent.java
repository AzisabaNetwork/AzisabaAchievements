package net.azisaba.azisabaachievements.spigot.event;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Fire this after an external {@code ProgressProvider} updates a player's progress.
 */
public final class ProgressUpdatedEvent extends AbstractEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final UUID playerId;

    public ProgressUpdatedEvent(@NotNull UUID playerId) {
        this.playerId = playerId;
    }

    @Contract(pure = true)
    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    @Contract(pure = true)
    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Contract(pure = true)
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
