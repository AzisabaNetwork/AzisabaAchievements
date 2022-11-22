package net.azisaba.azisabaachievements.common.network;

import net.azisaba.azisabaachievements.api.network.PacketRegistry;
import net.azisaba.azisabaachievements.api.network.PacketRegistryPair;
import org.jetbrains.annotations.NotNull;

public class PacketRegistryPairImpl implements PacketRegistryPair {
    private final PacketRegistry clientRegistry;
    private final PacketRegistry serverRegistry;

    public PacketRegistryPairImpl(@NotNull PacketRegistry clientRegistry, @NotNull PacketRegistry serverRegistry) {
        this.clientRegistry = clientRegistry;
        this.serverRegistry = serverRegistry;
    }

    @Override
    public @NotNull PacketRegistry getClientRegistry() {
        return clientRegistry;
    }

    @Override
    public @NotNull PacketRegistry getServerRegistry() {
        return serverRegistry;
    }
}
