package net.azisaba.azisabaachievements.velocity.plugin;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.azisaba.azisabaachievements.api.AzisabaAchievementsProvider;
import net.azisaba.azisabaachievements.api.AzisabaAchievementsProviderSetter;
import net.azisaba.azisabaachievements.api.Side;
import net.azisaba.azisabaachievements.api.network.PacketRegistry;
import net.azisaba.azisabaachievements.api.network.PacketRegistryPair;
import net.azisaba.azisabaachievements.common.network.PacketRegistryImpl;
import net.azisaba.azisabaachievements.common.redis.JedisBox;
import net.azisaba.azisabaachievements.common.sql.DatabaseManager;
import net.azisaba.azisabaachievements.velocity.command.AchievementsCommand;
import net.azisaba.azisabaachievements.velocity.listener.PlayerJoinListener;
import net.azisaba.azisabaachievements.velocity.redis.RedisConnectionLeader;
import net.azisaba.azisabaachievements.velocity.redis.ServerIdProvider;
import net.azisaba.azisabaachievements.velocity.VelocityAzisabaAchievements;
import net.azisaba.azisabaachievements.velocity.network.VelocityPacketListener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

@Plugin(id = "azisaba-achievements", name = "AzisabaAchievements", version = "@YOU_SHOULD_NOT_SEE_THIS_AS_VERSION@")
public class VelocityPlugin implements PacketRegistryPair {
    private final PacketRegistry clientRegistry = new PacketRegistryImpl();
    private final PacketRegistry serverRegistry = new PacketRegistryImpl();
    private final VelocityPacketListener packetListener = new VelocityPacketListener(this);
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final PluginConfig config;
    private final JedisBox jedisBox;
    private final ServerIdProvider serverIdProvider;
    private final RedisConnectionLeader redisConnectionLeader;
    private final DatabaseManager databaseManager;

    @Inject
    public VelocityPlugin(@NotNull ProxyServer server, @NotNull Logger logger, @NotNull @DataDirectory Path dataDirectory) throws SQLException {
        registerPackets();
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        config = loadConfig();
        jedisBox = createJedisBox();
        serverIdProvider = new ServerIdProvider(jedisBox.getJedisPool());
        redisConnectionLeader = new RedisConnectionLeader(jedisBox.getJedisPool(), serverIdProvider);
        databaseManager = new DatabaseManager(config.databaseConfig.createDataSource());
        AzisabaAchievementsProviderSetter.setInstance(new VelocityAzisabaAchievements(this));
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent e) {
        server.getCommandManager().register(new AchievementsCommand(databaseManager).createCommand());

        serverIdProvider.runIdKeeperTask(AzisabaAchievementsProvider.get().getScheduler());
        logger.info("This proxy's ID is " + serverIdProvider.getId());

        redisConnectionLeader.trySwitch();

        server.getScheduler()
                .buildTask(this, () -> {
                    if (redisConnectionLeader.isLeader()) {
                        redisConnectionLeader.extendLeaderExpire();
                    } else {
                        redisConnectionLeader.trySwitch();
                    }
                })
                .repeat(5, TimeUnit.SECONDS)
                .schedule();

        server.getEventManager().register(this, new PlayerJoinListener(databaseManager));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent e) {
        redisConnectionLeader.leaveLeader();
        serverIdProvider.deleteProxyId();
        try {
            Thread.sleep(500); // Wait for the leader to be deleted
        } catch (InterruptedException ignored) {}
        jedisBox.close();
        databaseManager.close();
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
                Side.PROXY,
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

    @NotNull
    public ServerIdProvider getServerIdProvider() {
        return serverIdProvider;
    }

    @NotNull
    public RedisConnectionLeader getRedisConnectionLeader() {
        return redisConnectionLeader;
    }

    @NotNull
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    private void registerPackets() {
        clientRegistry.registerCommonPackets();
        clientRegistry.registerProxyBoundPackets();
        serverRegistry.registerCommonPackets();
        serverRegistry.registerServerBoundPackets();
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
