package com.jason54.unpredictableblocks.world;

import dev.architectury.event.events.common.ChunkEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

public final class LuckyBlockSpawnHooks {
    private LuckyBlockSpawnHooks() {}

    public static void register() {
        ChunkEvent.LOAD.register((levelChunk, level, newChunk) -> {
            if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
                return;
            }
            var chunkPos = levelChunk.getPos();
            ChunkSpawnMarkers markers = ChunkSpawnMarkers.get(serverLevel);
            if (markers.isPopulated(chunkPos)) {
                return;
            }
            LuckyBlockSpawnPlacement.populateChunk(serverLevel, levelChunk);
            markers.markPopulated(chunkPos);
        });
    }
}
