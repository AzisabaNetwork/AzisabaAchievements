package net.azisaba.azisabaachievements.test.api.network;

import io.netty.buffer.Unpooled;
import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.PlayerAchievementData;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyRequestProgress;
import net.azisaba.azisabaachievements.api.network.packet.PacketProxySetAchievementProgress;
import net.azisaba.azisabaachievements.api.network.packet.PacketServerProgressResult;
import net.azisaba.azisabaachievements.api.util.Either;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProgressPacketTest {
    @Test
    public void setProgressPacketRoundTrips() {
        UUID playerId = UUID.randomUUID();
        PacketProxySetAchievementProgress original =
                new PacketProxySetAchievementProgress(playerId, Key.key("event:test"), 7, "server-a");
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        original.write(buffer);

        PacketProxySetAchievementProgress decoded = new PacketProxySetAchievementProgress(buffer);
        assertEquals(original.getSeq(), decoded.getSeq());
        assertEquals(playerId, decoded.getUniqueId());
        assertEquals(Key.key("event:test"), decoded.getKey());
        assertEquals(7, decoded.getCount());
        assertEquals("server-a", decoded.getOriginServerId());
    }

    @Test
    public void requestProgressPacketRoundTrips() {
        UUID playerId = UUID.randomUUID();
        PacketProxyRequestProgress original = new PacketProxyRequestProgress(playerId);
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        original.write(buffer);

        PacketProxyRequestProgress decoded = new PacketProxyRequestProgress(buffer);
        assertEquals(original.getSeq(), decoded.getSeq());
        assertEquals(playerId, decoded.getPlayerId());
    }

    @Test
    public void progressResultPacketRoundTrips() {
        UUID seq = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        PlayerAchievementData data = new PlayerAchievementData(playerId, Key.key("event:test"), 5);
        PacketServerProgressResult original =
                new PacketServerProgressResult(seq, Either.right(Collections.singletonList(data)));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        original.write(buffer);

        PacketServerProgressResult decoded = new PacketServerProgressResult(buffer);
        assertEquals(seq, decoded.getSeq());
        assertTrue(decoded.getResult().isRight());
        List<PlayerAchievementData> list = decoded.getResult().getRight();
        assertEquals(1, list.size());
        assertEquals(Key.key("event:test"), list.get(0).getAchievementKey());
        assertEquals(5, list.get(0).getCount());
    }

    @Test
    public void progressResultPacketPreservesError() {
        UUID seq = UUID.randomUUID();
        PacketServerProgressResult original = new PacketServerProgressResult(seq, Either.left("boom"));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        original.write(buffer);

        PacketServerProgressResult decoded = new PacketServerProgressResult(buffer);
        assertTrue(decoded.getResult().isLeft());
        assertEquals("boom", decoded.getResult().getLeft());
    }
}
