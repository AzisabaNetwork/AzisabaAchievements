package net.azisaba.azisabaachievements.common;

import net.azisaba.azisabaachievements.api.AzisabaAchievements;
import net.azisaba.azisabaachievements.api.Logger;
import net.azisaba.azisabaachievements.api.network.PacketRegistryPair;
import net.azisaba.azisabaachievements.api.network.PacketSender;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractAzisabaAchievements implements AzisabaAchievements {
    protected final Logger logger;
    protected final PacketRegistryPair packetRegistryPair;
    protected final PacketSender packetSender;

    public AbstractAzisabaAchievements(
            @NotNull Logger logger,
            @NotNull PacketRegistryPair packetRegistryPair,
            @NotNull PacketSender packetSender) {
        this.logger = logger;
        this.packetRegistryPair = packetRegistryPair;
        this.packetSender = packetSender;
    }

    @Override
    public @NotNull Logger getLogger() {
        return logger;
    }

    @Override
    public @NotNull PacketRegistryPair getPacketRegistryPair() {
        return packetRegistryPair;
    }

    @NotNull
    @Override
    public PacketSender getPacketSender() {
        return packetSender;
    }
}
