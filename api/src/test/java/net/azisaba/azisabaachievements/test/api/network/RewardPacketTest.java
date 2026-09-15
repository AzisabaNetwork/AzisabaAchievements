package net.azisaba.azisabaachievements.test.api.network;

import io.netty.buffer.Unpooled;
import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyProgressAchievement;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyUnlockAchievements;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerRewardDeliveries;
import net.azisaba.azisabaachievements.api.reward.RewardDelivery;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RewardPacketTest {
    @Test
    public void progressPacketPreservesOriginServer() {
        UUID playerId = UUID.randomUUID();
        PacketProxyProgressAchievement original =
                new PacketProxyProgressAchievement(playerId, Key.key("event:test"), 3, "server-a");
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        original.write(buffer);

        PacketProxyProgressAchievement decoded = new PacketProxyProgressAchievement(buffer);
        assertEquals(playerId, decoded.getUniqueId());
        assertEquals(Key.key("event:test"), decoded.getKey());
        assertEquals(3, decoded.getCount());
        assertEquals("server-a", decoded.getOriginServerId());
    }

    @Test
    public void unlockPacketPreservesKeys() {
        UUID playerId = UUID.randomUUID();
        PacketProxyUnlockAchievements original = new PacketProxyUnlockAchievements(
                playerId,
                "server-a",
                Arrays.asList(Key.key("azisaba:general/kill"), Key.key("azisaba:general/play"))
        );
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        original.write(buffer);

        PacketProxyUnlockAchievements decoded = new PacketProxyUnlockAchievements(buffer);
        assertEquals(playerId, decoded.getPlayerId());
        assertEquals("server-a", decoded.getOriginServerId());
        assertEquals(2, decoded.getKeys().size());
        assertEquals(Key.key("azisaba:general/kill"), decoded.getKeys().get(0));
        assertEquals(Key.key("azisaba:general/play"), decoded.getKeys().get(1));
    }

    @Test
    public void rewardDeliveriesPacketPreservesCommands() {
        UUID playerId = UUID.randomUUID();
        RewardDelivery delivery = new RewardDelivery(42, Key.key("event:test"), "give {player} diamond 1");
        PacketServerRewardDeliveries original =
                new PacketServerRewardDeliveries(playerId, "server-b", Collections.singleton(delivery));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        original.write(buffer);

        PacketServerRewardDeliveries decoded = new PacketServerRewardDeliveries(buffer);
        assertEquals(playerId, decoded.getPlayerId());
        assertEquals("server-b", decoded.getServerId());
        assertEquals(1, decoded.getDeliveries().size());
        assertEquals(42, decoded.getDeliveries().get(0).getId());
        assertEquals("give {player} diamond 1", decoded.getDeliveries().get(0).getCommand());
    }
}
