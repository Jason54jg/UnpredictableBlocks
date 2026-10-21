package com.jason54.unpredictableblocks.world;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

import java.util.HashSet;
import java.util.Set;

/** Évite de re-peupler les chunks déjà traités au chargement. */
public final class ChunkSpawnMarkers extends SavedData {
    private static final String TAG_CHUNKS = "Chunks";

    private static final Codec<ChunkSpawnMarkers> CODEC = CompoundTag.CODEC.xmap(ChunkSpawnMarkers::fromTag, ChunkSpawnMarkers::toTag);

    public static final SavedDataType<ChunkSpawnMarkers> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, "chunk_spawns"),
            ChunkSpawnMarkers::new,
            CODEC,
            DataFixTypes.SAVED_DATA_SCOREBOARD
    );

    private final Set<Long> populated = new HashSet<>();

    private static ChunkSpawnMarkers fromTag(CompoundTag tag) {
        ChunkSpawnMarkers data = new ChunkSpawnMarkers();
        ListTag list = tag.getListOrEmpty(TAG_CHUNKS);
        for (int i = 0; i < list.size(); i++) {
            Tag entry = list.get(i);
            if (entry instanceof NumericTag numericTag) {
                data.populated.add(numericTag.longValue());
            }
        }
        return data;
    }

    private CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Long chunkKey : populated) {
            list.add(LongTag.valueOf(chunkKey));
        }
        tag.put(TAG_CHUNKS, list);
        return tag;
    }

    public static ChunkSpawnMarkers get(ServerLevel level) {
        SavedDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(TYPE);
    }

    public boolean isPopulated(ChunkPos pos) {
        return populated.contains(pos.pack());
    }

    public void markPopulated(ChunkPos pos) {
        populated.add(pos.pack());
        setDirty();
    }
}
