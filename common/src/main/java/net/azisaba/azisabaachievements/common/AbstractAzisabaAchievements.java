package net.azisaba.azisabaachievements.common;

import net.azisaba.azisabaachievements.api.AzisabaAchievements;
import net.azisaba.azisabaachievements.api.Logger;
import net.azisaba.azisabaachievements.api.network.PacketRegistryPair;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractAzisabaAchievements implements AzisabaAchievements {
    protected final Logger logger;
    protected final PacketRegistryPair packetRegistryPair;

    public AbstractAzisabaAchievements(@NotNull Logger logger, @NotNull PacketRegistryPair packetRegistryPair) {
        this.logger = logger;
        this.packetRegistryPair = packetRegistryPair;
    }

    @Override
    public @NotNull Logger getLogger() {
        return logger;
    }

    @Override
    public @NotNull PacketRegistryPair getPacketRegistryPair() {
        return packetRegistryPair;
    }
}
