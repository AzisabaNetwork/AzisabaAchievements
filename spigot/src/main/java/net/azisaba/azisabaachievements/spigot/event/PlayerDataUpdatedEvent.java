package net.azisaba.azisabaachievements.spigot.event;

import net.azisaba.azisabaachievements.api.achievement.PlayerAchievementData;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Fired when the player data of one or more players is received.
 * @see net.azisaba.azisabaachievements.api.network.packet.PacketProxyRequestPlayerData
 * @see net.azisaba.azisabaachievements.api.network.packet.PacketServerPlayerData
 */
public final class PlayerDataUpdatedEvent extends AsyncEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Collection<PlayerAchievementData> playerAchievementData;

    public PlayerDataUpdatedEvent(@NotNull Collection<@NotNull PlayerAchievementData> playerAchievementData) {
        this.playerAchievementData = playerAchievementData;
    }

    @Contract(pure = true)
    public @NotNull Collection<@NotNull PlayerAchievementData> getPlayerAchievementData() {
        return playerAchievementData;
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
