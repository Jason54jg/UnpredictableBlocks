package com.jason54.unpredictableblocks.world;

import dev.architectury.event.events.common.ChunkEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
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
 * (ou même un accès {@code SavedData}) pendant le LOAD provoque un deadlock NeoForge
 * (freeze sur « Selecting global world spawn… » / « Chargement du monde »).
 * <p>
 * Le drain attend un warm-up après {@link LifecycleEvent#SERVER_STARTED} pour laisser
 * le client finir le loading terrain, puis tourne en fin de tick avec un budget limité.
 */
public final class LuckyBlockSpawnHooks {
    private static final Queue<PendingChunk> PENDING = new ArrayDeque<>();
    private static final Set<Long> PENDING_KEYS = new HashSet<>();
    /** Budget bas : NeoForge gèle si trop de setBlock/tick pendant le join. */
    private static final int PER_TICK_BUDGET = 4;
    private static final int WARMUP_TICKS = 80;
    private static volatile boolean serverReady;
    private static int ticksSinceReady;

    private LuckyBlockSpawnHooks() {}

    public static void register() {
        LifecycleEvent.SERVER_STARTED.register(server -> {
            serverReady = true;
            ticksSinceReady = 0;
        });
        TickEvent.SERVER_POST.register(LuckyBlockSpawnHooks::drainPending);
        LifecycleEvent.SERVER_STOPPED.register(server -> {
            serverReady = false;
            ticksSinceReady = 0;
            synchronized (PENDING) {
                PENDING.clear();
                PENDING_KEYS.clear();
            }
        });

        ChunkEvent.LOAD.register((levelChunk, level, newChunk) -> {
            if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
                return;
            }
            // Pas de SavedData / setBlock ici — uniquement l'enqueue.
            ChunkPos chunkPos = levelChunk.getPos();
            long key = pendingKey(serverLevel.dimension(), chunkPos);
            synchronized (PENDING) {
                if (!PENDING_KEYS.add(key)) {
                    return;
                }
                PENDING.add(new PendingChunk(serverLevel.dimension(), chunkPos.x(), chunkPos.z(), key));
            }
        });
    }

    private static void drainPending(MinecraftServer server) {
        if (!serverReady) {
            return;
        }
        if (ticksSinceReady < WARMUP_TICKS) {
            ticksSinceReady++;
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
