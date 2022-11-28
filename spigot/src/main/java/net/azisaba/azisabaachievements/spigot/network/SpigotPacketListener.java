package net.azisaba.azisabaachievements.spigot.network;

import net.azisaba.azisabaachievements.api.AzisabaAchievementsProvider;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.PlayerAchievementData;
import net.azisaba.azisabaachievements.api.network.ServerPacketListener;
import net.azisaba.azisabaachievements.api.network.packet.PacketCommonAchievementUnlocked;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerAddAchievementTranslation;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerCreateAchievementCallback;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerDataResult;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerFetchAchievementCallback;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerPlayerData;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerProgressAchievementCallback;
import net.azisaba.azisabaachievements.spigot.achievement.SpigotAchievementManager;
import net.azisaba.azisabaachievements.spigot.data.AchievementDataCache;
import net.azisaba.azisabaachievements.spigot.data.TranslatedAchievement;
import net.azisaba.azisabaachievements.spigot.event.AchievementUnlockedEvent;
import net.azisaba.azisabaachievements.spigot.event.AsyncEvent;
import net.azisaba.azisabaachievements.spigot.event.PlayerDataUpdatedEvent;
import net.azisaba.azisabaachievements.spigot.plugin.SpigotPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SpigotPacketListener implements ServerPacketListener {
    private final SpigotPlugin plugin;

    public SpigotPacketListener(@NotNull SpigotPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(@NotNull PacketCommonAchievementUnlocked packet) {
        AsyncEvent.call(new AchievementUnlockedEvent(packet.getPlayerUniqueId(), packet.getAchievement()));
    }

    @Override
    public void handle(@NotNull PacketServerDataResult packet) {
        AchievementDataCache cache = plugin.getAchievementDataCache();
        if (!cache.isPending()) {
            return;
        }
        cache.load(packet);
    }

    @Override
    public void handle(@NotNull PacketServerCreateAchievementCallback packet) {
        // add to cache
        if (packet.getResult().isRight()) {
            AchievementDataCache cache = plugin.getAchievementDataCache();
            cache.add(new TranslatedAchievement(packet.getResult().getRight(), new HashMap<>()));
        }

        // process callback
        CompletableFuture<AchievementData> cb = ((SpigotAchievementManager) AzisabaAchievementsProvider.get().getAchievementManager())
                .achievementDataCallback
                .remove(packet.getSeq());
        if (cb == null) {
            return;
        }
        if (packet.getResult().isLeft()) {
            cb.completeExceptionally(new RuntimeException("Proxy returned an error: " + packet.getResult().getLeft()));
        } else {
            cb.complete(packet.getResult().getRight());
        }
    }

    @Override
    public void handle(@NotNull PacketServerFetchAchievementCallback packet) {
        // add to cache
        if (packet.getResult().isRight()) {
            AchievementDataCache cache = plugin.getAchievementDataCache();
            Optional<AchievementData> opt = packet.getResult().getRight();
            opt.ifPresent(data ->
                    cache.getAchievements()
                            .computeIfAbsent(data.getKey(), k -> new TranslatedAchievement(data, new HashMap<>()))
                            .replaceData(data)
            );
        }

        // process callback
        CompletableFuture<Optional<AchievementData>> cb = ((SpigotAchievementManager) AzisabaAchievementsProvider.get().getAchievementManager())
                .optionalAchievementDataCallback
                .remove(packet.getSeq());
        if (cb == null) {
            return;
        }
        if (packet.getResult().isLeft()) {
            cb.completeExceptionally(new RuntimeException("Proxy returned an error: " + packet.getResult().getLeft()));
        } else {
            cb.complete(packet.getResult().getRight());
        }
    }

    @Override
    public void handle(@NotNull PacketServerProgressAchievementCallback packet) {
        CompletableFuture<Boolean> cb = ((SpigotAchievementManager) AzisabaAchievementsProvider.get().getAchievementManager())
                .progressAchievementCallback
                .remove(packet.getSeq());
        if (cb == null) {
            return;
        }
        if (packet.getResult().isLeft()) {
            cb.completeExceptionally(new RuntimeException("Proxy returned an error: " + packet.getResult().getLeft()));
        } else {
            cb.complete(packet.getResult().getRight());
        }
    }

    @Override
    public void handle(@NotNull PacketServerAddAchievementTranslation packet) {
        AchievementDataCache cache = plugin.getAchievementDataCache();
        TranslatedAchievement achievement = cache.getAchievement(packet.getData().getAchievementKey());
        achievement.addTranslation(packet.getData());
    }

    @Override
    public void handle(@NotNull PacketServerPlayerData packet) {
        AsyncEvent.call(new PlayerDataUpdatedEvent(packet.getData()));
        AchievementDataCache cache = plugin.getAchievementDataCache();
        for (PlayerAchievementData data : packet.getData()) {
            cache.add(data);
        }
    }
}
