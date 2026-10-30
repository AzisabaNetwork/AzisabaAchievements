package net.azisaba.azisabaachievements.spigot.achievement;

import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.PlayerAchievementData;
import net.azisaba.azisabaachievements.api.achievement.ProgressProvider;
import net.azisaba.azisabaachievements.api.network.PacketSender;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyProgressAchievement;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyRequestProgress;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxySetAchievementProgress;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerProgressAchievementCallback;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerProgressResult;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Built-in {@link ProgressProvider} that stores progress in the {@code player_achievements} table through the proxy.
 * It is registered with the lowest priority, so any external provider takes precedence. All methods block until the
 * proxy responds, so they must not be called from the main thread.
 */
public final class DatabaseProgressProvider implements ProgressProvider {
    private static final long TIMEOUT_SECONDS = 10;

    private final Map<UUID, CompletableFuture<Map<Key, Long>>> progressCallback = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<Boolean>> updateCallback = new ConcurrentHashMap<>();
    private final PacketSender packetSender;
    private final String serverId;

    public DatabaseProgressProvider(@NotNull PacketSender packetSender, @NotNull String serverId) {
        this.packetSender = packetSender;
        this.serverId = serverId;
    }

    @Override
    public @NotNull Map<Key, Long> getProgress(@NotNull UUID playerId) {
        PacketProxyRequestProgress packet = new PacketProxyRequestProgress(playerId);
        CompletableFuture<Map<Key, Long>> future = new CompletableFuture<>();
        progressCallback.put(packet.getSeq(), future);
        packetSender.sendPacket(packet);
        return await(progressCallback, packet.getSeq(), future);
    }

    @Override
    public void addProgress(@NotNull UUID playerId, @NotNull Key key, long amount) {
        PacketProxyProgressAchievement packet = new PacketProxyProgressAchievement(playerId, key, amount, serverId);
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        updateCallback.put(packet.getSeq(), future);
        packetSender.sendPacket(packet);
        await(updateCallback, packet.getSeq(), future);
    }

    @Override
    public void setProgress(@NotNull UUID playerId, @NotNull Key key, long count) {
        PacketProxySetAchievementProgress packet = new PacketProxySetAchievementProgress(playerId, key, count, serverId);
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        updateCallback.put(packet.getSeq(), future);
        packetSender.sendPacket(packet);
        await(updateCallback, packet.getSeq(), future);
    }

    public void complete(@NotNull PacketServerProgressResult packet) {
        CompletableFuture<Map<Key, Long>> future = progressCallback.remove(packet.getSeq());
        if (future == null) {
            return;
        }
        if (packet.getResult().isLeft()) {
            future.completeExceptionally(new RuntimeException("Proxy returned an error: " + packet.getResult().getLeft()));
            return;
        }
        Map<Key, Long> progress = new HashMap<>();
        for (PlayerAchievementData data : packet.getResult().getRight()) {
            progress.put(data.getAchievementKey(), data.getCount());
        }
        future.complete(progress);
    }

    public void complete(@NotNull PacketServerProgressAchievementCallback packet) {
        CompletableFuture<Boolean> future = updateCallback.remove(packet.getSeq());
        if (future == null) {
            return;
        }
        if (packet.getResult().isLeft()) {
            future.completeExceptionally(new RuntimeException("Proxy returned an error: " + packet.getResult().getLeft()));
        } else {
            future.complete(packet.getResult().getRight());
        }
    }

    private static <T> T await(
            @NotNull Map<UUID, CompletableFuture<T>> callbacks,
            @NotNull UUID seq,
            @NotNull CompletableFuture<T> future
    ) {
        try {
            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for the proxy", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e.getCause().getMessage(), e.getCause());
        } catch (TimeoutException e) {
            throw new IllegalStateException("Timed out waiting for the proxy", e);
        } finally {
            callbacks.remove(seq);
        }
    }
}
