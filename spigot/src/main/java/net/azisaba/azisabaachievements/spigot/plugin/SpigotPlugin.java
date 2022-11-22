package net.azisaba.azisabaachievements.spigot.plugin;

import net.azisaba.azisabaachievements.api.AzisabaAchievementsProviderSetter;
import net.azisaba.azisabaachievements.api.network.PacketListener;
import net.azisaba.azisabaachievements.api.network.PacketRegistry;
import net.azisaba.azisabaachievements.api.network.PacketRegistryPair;
import net.azisaba.azisabaachievements.common.network.PacketRegistryImpl;
import net.azisaba.azisabaachievements.common.redis.JedisBox;
import net.azisaba.azisabaachievements.spigot.SpigotAzisabaAchievements;
import net.azisaba.azisabaachievements.spigot.command.AzisabaAchievementsCommand;
import net.azisaba.azisabaachievements.spigot.network.SpigotPacketListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SpigotPlugin extends JavaPlugin implements PacketRegistryPair {
    private final PacketRegistry clientRegistry = new PacketRegistryImpl();
    private final PacketRegistry serverRegistry = new PacketRegistryImpl();
    private final PacketListener packetListener = new SpigotPacketListener();
    private PluginConfig pluginConfig;
    private JedisBox jedisBox;

    public SpigotPlugin() {
        clientRegistry.registerCommonPackets();
        clientRegistry.registerProxyPackets();
        serverRegistry.registerCommonPackets();
        serverRegistry.registerServerPackets();
    }

    @Override
    public void onLoad() {
        saveDefaultConfig();
        this.pluginConfig = new PluginConfig(getConfig());
        this.jedisBox = createJedisBox();
        AzisabaAchievementsProviderSetter.setInstance(new SpigotAzisabaAchievements(this));
    }

    @Override
    public void onEnable() {
        Objects.requireNonNull(Bukkit.getPluginCommand("azisabaachievements"))
                .setExecutor(new AzisabaAchievementsCommand());
    }

    @Contract(" -> new")
    private @NotNull JedisBox createJedisBox() {
        return new JedisBox(
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
        return Objects.requireNonNull(pluginConfig, "PluginConfig is not loaded yet");
    }

    public JedisBox getJedisBox() {
        return Objects.requireNonNull(jedisBox, "jedisBox is not initialized yet");
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
