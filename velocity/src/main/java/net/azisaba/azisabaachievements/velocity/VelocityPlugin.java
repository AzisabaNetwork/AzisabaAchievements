package net.azisaba.azisabaachievements.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@Plugin(id = "azisaba-achievements", name = "AzisabaAchievements", version = "@YOU_SHOULD_NOT_SEE_THIS_AS_VERSION@")
public class VelocityPlugin {
    @Inject
    public VelocityPlugin(@NotNull ProxyServer server, @NotNull Logger logger) {
    }
}
