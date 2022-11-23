package net.azisaba.azisabaachievements.velocity.redis;

import net.azisaba.azisabaachievements.api.AzisabaAchievementsProvider;
import net.azisaba.azisabaachievements.api.Logger;
import net.azisaba.azisabaachievements.api.network.packet.PacketCommonProxyLeaderChanged;
import net.azisaba.azisabaachievements.api.network.packet.PacketCommonProxyLeaderLeave;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Objects;

public class RedisConnectionLeader {
    private static final String REDIS_KEY = "azisaba-achievements:connection-leader";
    private final JedisPool jedisPool;
    private final ServerIdProvider serverIdProvider;

    private boolean leader = false;
    private long leaderExpireAt = 0;

    public RedisConnectionLeader(@NotNull JedisPool jedisPool, @NotNull ServerIdProvider serverIdProvider) {
        this.jedisPool = jedisPool;
        this.serverIdProvider = serverIdProvider;
    }

    public boolean isLeader() {
        if (leader && leaderExpireAt < System.currentTimeMillis()) {
            leader = false;
        }
        return leader;
    }

    public boolean trySwitch() {
        try (Jedis jedis = jedisPool.getResource()) {
            String result = jedis.set(REDIS_KEY, serverIdProvider.getId(), SetParams.setParams().nx().ex(10));

            if (result != null) {
                leader = true;
                leaderExpireAt = System.currentTimeMillis() + (10 * 1000);

                Logger.getCurrentLogger().info("This proxy was selected as a new leader.");
                AzisabaAchievementsProvider.get().getPacketSender()
                        .sendPacket(new PacketCommonProxyLeaderChanged(serverIdProvider.getId()));
                jedis.publish(REDIS_KEY, serverIdProvider.getId());
                return true;
            } else {
                String currentLeader = jedis.get(REDIS_KEY);
                if (Objects.equals(serverIdProvider.getId(), currentLeader)) {
                    leader = true;
                    return true;
                }

                leader = false;
                return false;
            }
        }
    }

    public void extendLeaderExpire() {
        if (!trySwitch()) {
            return;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.expire(REDIS_KEY, 10);
            leaderExpireAt = System.currentTimeMillis() + (10 * 1000);
        }
    }

    public void leaveLeader() {
        try (Jedis jedis = jedisPool.getResource()) {
            String currentLeader = jedis.get(REDIS_KEY);
            if (!Objects.equals(serverIdProvider.getId(), currentLeader)) {
                return;
            }

            jedis.del(REDIS_KEY);
            AzisabaAchievementsProvider.get()
                    .getPacketSender()
                    .sendPacket(new PacketCommonProxyLeaderLeave(serverIdProvider.getId()));
        }
    }
}
