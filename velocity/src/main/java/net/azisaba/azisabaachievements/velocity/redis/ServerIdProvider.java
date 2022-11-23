package net.azisaba.azisabaachievements.velocity.redis;

import net.azisaba.azisabaachievements.api.scheduler.TaskScheduler;
import net.azisaba.azisabaachievements.common.util.RandomUtil;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ServerIdProvider {
    private final JedisPool jedisPool;
    private String id;

    public ServerIdProvider(@NotNull JedisPool jedisPool) {
        this.jedisPool = Objects.requireNonNull(jedisPool, "jedisPool");
    }

    /**
     * Get the server id of this server and obtains the new id if it is not obtained yet.
     * @return the server id
     */
    @NotNull
    public String getId() {
        if (id != null) {
            return id;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            String idTmp = null;
            while (idTmp == null) {
                idTmp = RandomUtil.randomAlphanumeric(8);
                String key = "azisaba-achievements:server-id:" + idTmp;

                String result = jedis.set(key, "using", SetParams.setParams().nx().ex(300));

                if (result == null) {
                    idTmp = null;
                }
            }

            id = idTmp;
        }
        return id;
    }

    /**
     * Server ID keys are automatically deleted after 5 minutes. This method is used to keep the key as long as the
     * server is running.
     * @param scheduler the scheduler
     */
    public void runIdKeeperTask(@NotNull TaskScheduler scheduler) {
        scheduler.builder(this::extendExpirationTime).repeat(2, TimeUnit.MINUTES).schedule();
    }

    public void extendExpirationTime() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.expire("azisaba-achievements:server-id:" + id, 300);
        }
    }

    public void deleteProxyId() {
        if (id == null) {
            return;
        }

        id = null;

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del("azisaba-achievements:server-id:" + id);
        }
    }
}
