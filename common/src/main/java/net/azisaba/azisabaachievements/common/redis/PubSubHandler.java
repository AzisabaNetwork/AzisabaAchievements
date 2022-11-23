package net.azisaba.azisabaachievements.common.redis;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.azisaba.azisabaachievements.api.Logger;
import net.azisaba.azisabaachievements.api.Side;
import net.azisaba.azisabaachievements.api.network.Packet;
import net.azisaba.azisabaachievements.api.network.PacketByteBuf;
import net.azisaba.azisabaachievements.api.network.PacketListener;
import net.azisaba.azisabaachievements.api.network.PacketRegistry;
import net.azisaba.azisabaachievements.api.network.PacketRegistryPair;
import net.azisaba.azisabaachievements.api.network.PacketSender;
import net.azisaba.azisabaachievements.common.util.ByteBufUtil;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PubSubHandler implements Closeable, PacketSender {
    private static final String CHANNEL_STRING = "azisaba-achievements:pubsub";
    public static final byte @NotNull [] CHANNEL = CHANNEL_STRING.getBytes(StandardCharsets.UTF_8);
    private final ArrayDeque<Consumer<byte[]>> pingPongQueue = new ArrayDeque<>();
    private final PubSubListener listener = new PubSubListener();
    private final Side side;
    private final Logger logger;
    private final JedisPool jedisPool;
    private final PacketListener packetListener;
    private final PacketRegistry serverRegistry;
    private final PacketRegistry clientRegistry;
    private final ScheduledExecutorService pingThread = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "AzisabaAchievements PubSub Ping Thread");
        t.setDaemon(true);
        return t;
    });
    private final ExecutorService subscriberThread = Executors.newFixedThreadPool(1, r -> {
        Thread t = new Thread(r, "AzisabaAchievements PubSub Subscriber Thread");
        t.setDaemon(true);
        return t;
    });

    public PubSubHandler(
            @NotNull Side side,
            @NotNull Logger logger,
            @NotNull JedisPool jedisPool,
            @NotNull PacketListener packetListener,
            @NotNull PacketRegistryPair packetRegistryPair
    ) {
        this.side = side;
        this.logger = logger;
        this.jedisPool = jedisPool;
        this.packetListener = packetListener;
        this.serverRegistry = packetRegistryPair.getServerRegistry();
        this.clientRegistry = packetRegistryPair.getClientRegistry();
        register();
    }

    private void loop() {
        try {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.subscribe(listener, CHANNEL);
            } catch (JedisConnectionException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            logger.warn("Failed to get Jedis resource", e);
        } finally {
            subscriberThread.submit(() -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                loop(); // recursion
            });
        }
    }

    private void register() {
        jedisPool.getResource().close(); // check connection
        subscriberThread.submit(this::loop);
        pingThread.scheduleAtFixedRate(() -> {
            try {
                long latency = ping();
                if (latency < 0) {
                    logger.warn("Got disconnected from Redis server, attempting to reconnect... (code: {})", latency);
                    listener.unsubscribe();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    @Override
    public void sendPacket(@NotNull Packet<?> packet) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        int packetId = serverRegistry.getId(packet.getClass());
        if (packetId == -1) {
            throw new IllegalArgumentException("Packet " + packet.getClass().getTypeName() + " is not registered");
        }
        if (clientRegistry.getId(packet.getClass()) == -1) {
            // sided packet
            buf.writeByte(side.ordinal());
        } else {
            buf.writeByte(-1);
        }
        buf.writeVarInt(packetId);
        packet.write(buf);
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(CHANNEL, ByteBufUtil.toByteArray(buf));
        }
    }

    private void processRawMessage(byte[] message) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.wrappedBuffer(message));
        int sideByte = buf.readByte();
        if (sideByte == side.ordinal()) {
            // ignore packets from same side
            return;
        }
        int packetId = buf.readVarInt();
        try {
            handlePacket(packetId, buf.slice());
        } catch (Exception e) {
            logger.error("Failed to process packet {}", packetId, e);
        } finally {
            if (buf.refCnt() > 0) {
                buf.release();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <P extends PacketListener> void handlePacket(int packetId, @NotNull ByteBuf buf) {
        Packet<P> packet = (Packet<P>) clientRegistry.createPacket(packetId, buf);
        try {
            packet.handle((P) packetListener);
        } catch (Exception e) {
            logger.error("Failed to handle packet {}/{}", packetId, packet.getClass().getTypeName(), e);
        }
    }

    private long ping() {
        if (!listener.isSubscribed()) {
            return -2;
        }

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        thread.start();
        long start = System.currentTimeMillis();

        pingPongQueue.add(arg -> thread.interrupt());
        try {
            listener.ping();
        } catch (JedisConnectionException e) {
            return -1;
        }

        try {
            thread.join(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return System.currentTimeMillis() - start;
    }

    @Override
    public void close() {
        subscriberThread.shutdownNow();
        pingThread.shutdownNow();
    }

    private class PubSubListener extends BinaryJedisPubSub {
        @Override
        public void onMessage(byte[] channel, byte[] message) {
            if (Arrays.equals(CHANNEL, channel)) {
                PubSubHandler.this.processRawMessage(message);
            }
        }

        @Override
        public void onPong(byte[] pattern) {
            Consumer<byte[]> consumer = pingPongQueue.poll();
            if (consumer != null) {
                try {
                    consumer.accept(pattern);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
