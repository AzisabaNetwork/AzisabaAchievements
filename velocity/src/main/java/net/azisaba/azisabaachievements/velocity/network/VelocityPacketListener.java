package net.azisaba.azisabaachievements.velocity.network;

import net.azisaba.azisabaachievements.api.AzisabaAchievementsProvider;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.AchievementTranslationData;
import net.azisaba.azisabaachievements.api.network.ProxyPacketListener;
import net.azisaba.azisabaachievements.api.network.packet.PacketCommonAchievementUnlocked;
import net.azisaba.azisabaachievements.api.network.packet.PacketCommonProxyLeaderChanged;
import net.azisaba.azisabaachievements.api.network.packet.PacketCommonProxyLeaderLeave;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyAddAchievementTranslation;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyCreateAchievement;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyFetchAchievement;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyProgressAchievement;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyRequestData;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerAddAchievementTranslation;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerCreateAchievementCallback;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerDataResult;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerFetchAchievementCallback;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerProgressAchievementCallback;
import net.azisaba.azisabaachievements.api.util.Either;
import net.azisaba.azisabaachievements.common.sql.DataProvider;
import net.azisaba.azisabaachievements.velocity.plugin.VelocityPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

public class VelocityPacketListener implements ProxyPacketListener {
    private final VelocityPlugin plugin;

    public VelocityPacketListener(@NotNull VelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(@NotNull PacketCommonProxyLeaderLeave packet) {
        if (!packet.getServerId().equals(plugin.getServerIdProvider().getId())) {
            plugin.getRedisConnectionLeader().trySwitch();
        }
    }

    @Override
    public void handle(@NotNull PacketCommonProxyLeaderChanged packet) {
        if (!packet.getServerId().equals(plugin.getServerIdProvider().getId())) {
            plugin.getRedisConnectionLeader().trySwitch();
        }
    }

    @Override
    public void handle(@NotNull PacketProxyRequestData packet) {
        if (!plugin.getRedisConnectionLeader().isLeader()) {
            return;
        }
        AzisabaAchievementsProvider.get()
                .getScheduler()
                .builder(() -> {
                    Set<AchievementData> achievements = DataProvider.getAllAchievements(plugin.getDatabaseManager());
                    Set<AchievementTranslationData> translations = DataProvider.getAllTranslations(plugin.getDatabaseManager());
                    AzisabaAchievementsProvider.get().getPacketSender().sendPacket(new PacketServerDataResult(achievements, translations));
                }).async().schedule();
    }

    @Override
    public void handle(@NotNull PacketProxyCreateAchievement packet) {
        if (!plugin.getRedisConnectionLeader().isLeader()) {
            return;
        }
        AzisabaAchievementsProvider.get()
                .getAchievementManager()
                .createAchievement(packet.getKey(), packet.getCount(), packet.getPoint())
                .whenComplete((data, throwable) -> {
                    Either<String, AchievementData> either;
                    if (throwable != null) {
                        either = Either.left(throwable.getMessage());
                    } else {
                        either = Either.right(data);
                    }
                    AzisabaAchievementsProvider.get().getPacketSender().sendPacket(new PacketServerCreateAchievementCallback(packet.getSeq(), either));
                });
    }

    @Override
    public void handle(@NotNull PacketProxyFetchAchievement packet) {
        if (!plugin.getRedisConnectionLeader().isLeader()) {
            return;
        }
        AzisabaAchievementsProvider.get()
                .getAchievementManager()
                .getAchievement(packet.getKey())
                .whenComplete((data, throwable) -> {
                    Either<String, Optional<AchievementData>> either;
                    if (throwable != null) {
                        either = Either.left(throwable.getMessage());
                    } else {
                        either = Either.right(data);
                    }
                    AzisabaAchievementsProvider.get().getPacketSender().sendPacket(new PacketServerFetchAchievementCallback(packet.getSeq(), either));
                });
    }

    @Override
    public void handle(@NotNull PacketProxyProgressAchievement packet) {
        if (!plugin.getRedisConnectionLeader().isLeader()) {
            return;
        }
        AzisabaAchievementsProvider.get()
                .getAchievementManager()
                .progressAchievement(packet.getUniqueId(), packet.getKey(), packet.getCount())
                .whenComplete((result, throwable) -> {
                    Either<String, Boolean> either;
                    if (throwable != null) {
                        either = Either.left(throwable.getMessage());
                    } else {
                        either = Either.right(result);
                    }
                    AzisabaAchievementsProvider.get().getPacketSender().sendPacket(new PacketServerProgressAchievementCallback(packet.getSeq(), either));
                    if (result != null && result) {
                        AchievementData achievement = DataProvider.getAchievementByKey(plugin.getDatabaseManager(), packet.getKey());
                        if (achievement == null) {
                            throw new AssertionError("Achievement " + packet.getKey() + " is missing");
                        }
                        AzisabaAchievementsProvider.get().getPacketSender().sendPacket(new PacketCommonAchievementUnlocked(packet.getUniqueId(), achievement));
                    }
                });
    }

    @Override
    public void handle(@NotNull PacketProxyAddAchievementTranslation packet) {
        if (!plugin.getRedisConnectionLeader().isLeader()) {
            return;
        }
        try {
            plugin.getDatabaseManager().queryVoid("INSERT INTO `achievement_translations` (`id`, `lang`, `name`, `description`) " +
                    "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `description` = VALUES(`description`)", ps -> {
                ps.setLong(1, packet.getData().getAchievementId());
                ps.setString(2, packet.getData().getLanguage());
                ps.setString(3, packet.getData().getName());
                ps.setString(4, packet.getData().getDescription());
                ps.executeUpdate();
                AzisabaAchievementsProvider.get().getPacketSender().sendPacket(new PacketServerAddAchievementTranslation(packet.getData()));
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
