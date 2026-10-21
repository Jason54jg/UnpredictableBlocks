package com.jason54.unpredictableblocks.world;

import dev.architectury.event.events.common.ChunkEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Enfile le peuplement hors de {@link ChunkEvent#LOAD} : un {@code setBlock}/{@code getBlockState}
 * pendant le LOAD provoque un deadlock (freeze sur « Selecting global world spawn… »).
 * Le drain ne démarre qu'après {@link LifecycleEvent#SERVER_STARTED}.
 */
public final class LuckyBlockSpawnHooks {
    private static final Queue<PendingChunk> PENDING = new ArrayDeque<>();
    private static final Set<Long> PENDING_KEYS = new HashSet<>();
    private static final int PER_TICK_BUDGET = 8;
    private static volatile boolean serverReady;

    private LuckyBlockSpawnHooks() {}

    public static void register() {
        LifecycleEvent.SERVER_STARTED.register(server -> {
            serverReady = true;
            server.execute(() -> drainPending(server));
        });
        LifecycleEvent.SERVER_STOPPED.register(server -> {
            serverReady = false;
            synchronized (PENDING) {
                PENDING.clear();
                PENDING_KEYS.clear();
            }
        });

        ChunkEvent.LOAD.register((levelChunk, level, newChunk) -> {
            if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
                return;
            }
            ChunkPos chunkPos = levelChunk.getPos();
            ChunkSpawnMarkers markers = ChunkSpawnMarkers.get(serverLevel);
            if (markers.isPopulated(chunkPos)) {
                return;
            }
            long key = pendingKey(serverLevel.dimension(), chunkPos);
            MinecraftServer server = serverLevel.getServer();
            synchronized (PENDING) {
                if (!PENDING_KEYS.add(key)) {
                    return;
                }
                PENDING.add(new PendingChunk(serverLevel.dimension(), chunkPos.x(), chunkPos.z(), key));
            }
            if (serverReady) {
                server.execute(() -> drainPending(server));
            }
        });
    }

    private static void drainPending(MinecraftServer server) {
        if (!serverReady) {
            return;
        }
        for (int i = 0; i < PER_TICK_BUDGET; i++) {
            PendingChunk next;
            synchronized (PENDING) {
                next = PENDING.poll();
                if (next == null) {
                    return;
                }
            }
            try {
                process(server, next);
            } finally {
                synchronized (PENDING) {
                    PENDING_KEYS.remove(next.key());
                }
            }
        }
        boolean more;
        synchronized (PENDING) {
            more = !PENDING.isEmpty();
        }
        if (more) {
            server.execute(() -> drainPending(server));
        }
    }

    private static void process(MinecraftServer server, PendingChunk pending) {
        ServerLevel level = server.getLevel(pending.dimension());
        if (level == null) {
            return;
        }
        ChunkPos chunkPos = new ChunkPos(pending.chunkX(), pending.chunkZ());
        ChunkSpawnMarkers markers = ChunkSpawnMarkers.get(level);
        if (markers.isPopulated(chunkPos)) {
            return;
        }
        LevelChunk chunk = level.getChunkSource().getChunkNow(pending.chunkX(), pending.chunkZ());
        if (chunk == null) {
            // Chunk déjà déchargé : on réessaiera au prochain LOAD.
            return;
        }
        LuckyBlockSpawnPlacement.populateChunk(level, chunk);
        markers.markPopulated(chunkPos);
    }

    private static long pendingKey(ResourceKey<Level> dimension, ChunkPos pos) {
        return (((long) dimension.identifier().hashCode()) << 32) ^ pos.pack();
    }

    private record PendingChunk(ResourceKey<Level> dimension, int chunkX, int chunkZ, long key) {}
}
