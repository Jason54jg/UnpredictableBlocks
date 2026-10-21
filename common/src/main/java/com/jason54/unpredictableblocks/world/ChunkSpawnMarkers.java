package com.jason54.unpredictableblocks.world;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashSet;
import java.util.Set;

/** Évite de re-peupler les chunks déjà traités au chargement. */
public final class ChunkSpawnMarkers extends SavedData {
    private static final String DATA_ID = UnpredictableBlocks.MOD_ID + "_chunk_spawns";
    private static final String TAG_CHUNKS = "Chunks";

    private final Set<Long> populated = new HashSet<>();

    static ChunkSpawnMarkers load(CompoundTag tag, HolderLookup.Provider registries) {
        ChunkSpawnMarkers data = new ChunkSpawnMarkers();
        ListTag list = tag.getList(TAG_CHUNKS, 8);
        for (int i = 0; i < list.size(); i++) {
            data.populated.add(list.getLong(i));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Long chunkKey : populated) {
            list.add(LongTag.valueOf(chunkKey));
        }
        tag.put(TAG_CHUNKS, list);
        return tag;
    }

    public static ChunkSpawnMarkers get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(new SavedData.Factory<>(ChunkSpawnMarkers::new, ChunkSpawnMarkers::load, null), DATA_ID);
    }

    public boolean isPopulated(ChunkPos pos) {
        return populated.contains(pos.toLong());
    }

    public void markPopulated(ChunkPos pos) {
        populated.add(pos.toLong());
        setDirty();
    }
}
