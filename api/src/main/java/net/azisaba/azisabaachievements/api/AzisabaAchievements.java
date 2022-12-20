package net.azisaba.azisabaachievements.api;

import net.azisaba.azisabaachievements.api.achievement.AchievementManager;
import net.azisaba.azisabaachievements.api.network.PacketRegistryPair;
import net.azisaba.azisabaachievements.api.network.PacketSender;
import net.azisaba.azisabaachievements.api.scheduler.TaskScheduler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface AzisabaAchievements {
    /**
     * Returns the logger instance. The logger can also be obtained by {@link Logger#getCurrentLogger()}.
     * @return the logger
     */
    @Contract(pure = true)
    @NotNull
    Logger getLogger();

    /**
     * Returns the packet registry pair. The server registry is used for sending packets, and the client
     * registry is used for processing packets sent from somewhere.
     * @return the packet registry pair
     */
    @Contract(pure = true)
    @NotNull
    PacketRegistryPair getPacketRegistryPair();

    /**
     * Returns the packet sender. The packet sender is used for sending the packet to the listener.
     * @return the packet sender
     */
    @Contract(pure = true)
    @NotNull
    PacketSender getPacketSender();

    /**
     * Returns the task scheduler. The task scheduler is used for scheduling tasks synchronously or asynchronously.
     * @return the task scheduler
     */
    @Contract(pure = true)
    @NotNull
    TaskScheduler getScheduler();

    /**
     * Returns the achievement manager.
     * @return the achievement manager
     */
    @Contract(pure = true)
    @NotNull
    AchievementManager getAchievementManager();

    /**
     * Creates a fastutil Int2ObjectHashMap instance.
     * @return the Int2ObjectHashMap instance
     * @throws UnsupportedOperationException if the fastutil library is not found
     */
    @SuppressWarnings("unchecked")
    default <T> @NotNull Map<@NotNull Integer, T> createInt2ObjectHashMap() throws UnsupportedOperationException {
        try {
            Class<?> clazz = Class.forName("net.azisaba.azisabaachievements.libs." + new String(new char[]{'i', 't', '.', 'u', 'n', 'i', 'm', 'i'}) + ".dsi.fastutil.ints.Int2ObjectOpenHashMap");
            return (Map<@NotNull Integer, T>) clazz.getConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new UnsupportedOperationException("fastutil library is not found", ex);
        }
    }
}
