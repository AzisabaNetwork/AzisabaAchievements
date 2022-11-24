package net.azisaba.azisabaachievements.spigot.plugin;

import net.azisaba.azisabaachievements.api.AzisabaAchievementsProviderSetter;
import net.azisaba.azisabaachievements.api.Side;
import net.azisaba.azisabaachievements.api.network.PacketListener;
import net.azisaba.azisabaachievements.api.network.PacketRegistry;
import net.azisaba.azisabaachievements.api.network.PacketRegistryPair;
import net.azisaba.azisabaachievements.common.network.PacketRegistryImpl;
import net.azisaba.azisabaachievements.common.redis.JedisBox;
import net.azisaba.azisabaachievements.spigot.SpigotAzisabaAchievements;
import net.azisaba.azisabaachievements.spigot.command.AzisabaAchievementsCommand;
import net.azisaba.azisabaachievements.spigot.data.AchievementDataCache;
import net.azisaba.azisabaachievements.spigot.network.SpigotPacketListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SpigotPlugin extends JavaPlugin implements PacketRegistryPair {
    private final PacketRegistry clientRegistry = new PacketRegistryImpl();
    private final PacketRegistry serverRegistry = new PacketRegistryImpl();
    private final PacketListener packetListener = new SpigotPacketListener(this);
    private PluginConfig pluginConfig;
    private JedisBox jedisBox;
    private AchievementDataCache achievementDataCache;

    public SpigotPlugin() {
        clientRegistry.registerCommonPackets();
        clientRegistry.registerServerBoundPackets();
        serverRegistry.registerCommonPackets();
        serverRegistry.registerProxyBoundPackets();
    }

    @Override
    public void onLoad() {
        saveDefaultConfig();
        pluginConfig = new PluginConfig(getConfig());
        jedisBox = createJedisBox();
        achievementDataCache = new AchievementDataCache(jedisBox.getPubSubHandler());
        AzisabaAchievementsProviderSetter.setInstance(new SpigotAzisabaAchievements(this));
    }

    @Override
    public void onEnable() {
        getLogger().info("Performing initial data load...");
        long start = System.currentTimeMillis();
        try {
            achievementDataCache.requestRefresh().get(3, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException("Failed to fetch data", e);
        }
        getLogger().info("Initial data load completed in " + (System.currentTimeMillis() - start) + " ms.");
        Objects.requireNonNull(Bukkit.getPluginCommand("azisabaachievements"))
                .setExecutor(new AzisabaAchievementsCommand(this));
    }

    @Contract(" -> new")
    private @NotNull JedisBox createJedisBox() {
        return new JedisBox(
                Side.SERVER,
                net.azisaba.azisabaachievements.api.Logger.createFromJavaLogger(getLogger()),
                packetListener,
                this,
                getPluginConfig().redisHost,
                getPluginConfig().redisPort,
                getPluginConfig().redisUsername,
                getPluginConfig().redisPassword
        );
    }

    @NotNull
    public PluginConfig getPluginConfig() {
        return Objects.requireNonNull(pluginConfig, "PluginConfig is not loaded");
    }

    @NotNull
    public JedisBox getJedisBox() {
        return Objects.requireNonNull(jedisBox, "jedisBox is not initialized");
    }

    @NotNull
    public AchievementDataCache getAchievementDataCache() {
        return Objects.requireNonNull(achievementDataCache, "AchievementDataCache is not initialized");
    }

    @NotNull
    @Override
    public PacketRegistry getClientRegistry() {
        return clientRegistry;
    }

    @NotNull
    @Override
    public PacketRegistry getServerRegistry() {
        return serverRegistry;
    }
}
