package net.azisaba.azisabaachievements.velocity.plugin;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.azisaba.azisabaachievements.api.AzisabaAchievementsProviderSetter;
import net.azisaba.azisabaachievements.api.network.PacketRegistry;
import net.azisaba.azisabaachievements.api.network.PacketRegistryPair;
import net.azisaba.azisabaachievements.common.network.PacketRegistryImpl;
import net.azisaba.azisabaachievements.common.redis.JedisBox;
import net.azisaba.azisabaachievements.velocity.VelocityAzisabaAchievements;
import net.azisaba.azisabaachievements.velocity.network.VelocityPacketListener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(id = "azisaba-achievements", name = "AzisabaAchievements", version = "@YOU_SHOULD_NOT_SEE_THIS_AS_VERSION@")
public class VelocityPlugin implements PacketRegistryPair {
    private final PacketRegistry clientRegistry = new PacketRegistryImpl();
    private final PacketRegistry serverRegistry = new PacketRegistryImpl();
    private final VelocityPacketListener packetListener = new VelocityPacketListener();
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final PluginConfig config;
    private final JedisBox jedisBox;

    @Inject
    public VelocityPlugin(@NotNull ProxyServer server, @NotNull Logger logger, @NotNull @DataDirectory Path dataDirectory) {
        registerPackets();
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.config = loadConfig();
        this.jedisBox = createJedisBox();
        AzisabaAchievementsProviderSetter.setInstance(new VelocityAzisabaAchievements(this));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent e) {
        jedisBox.close();
    }

    @Contract(" -> new")
    private @NotNull PluginConfig loadConfig() {
        try {
            return new PluginConfig(dataDirectory.resolve("config.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Contract(" -> new")
    private @NotNull JedisBox createJedisBox() {
        return new JedisBox(
                net.azisaba.azisabaachievements.api.Logger.createByProxy(logger),
                packetListener,
                this,
                config.redisHost,
                config.redisPort,
                config.redisUsername,
                config.redisPassword
        );
    }

    @NotNull
    public ProxyServer getServer() {
        return server;
    }

    @NotNull
    public Logger getLogger() {
        return logger;
    }

    @NotNull
    public JedisBox getJedisBox() {
        return jedisBox;
    }

    private void registerPackets() {
        clientRegistry.registerCommonPackets();
        clientRegistry.registerServerPackets();
        serverRegistry.registerCommonPackets();
        serverRegistry.registerProxyPackets();
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
