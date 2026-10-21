package com.jason54.unpredictableblocks.quest;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Compteurs de quêtes (BONK, etc.) par joueur. */
public final class PlayerQuestData {
    private static final String DATA_ID = UnpredictableBlocks.MOD_ID + "_quests";

    private PlayerQuestData() {}

    public static int getBonkCount(ServerPlayer player) {
        return root(player).bonkCounts.getOrDefault(player.getUUID(), 0);
    }

    public static int incrementBonk(ServerPlayer player) {
        QuestSavedData data = root(player);
        int next = data.bonkCounts.getOrDefault(player.getUUID(), 0) + 1;
        data.bonkCounts.put(player.getUUID(), next);
        data.setDirty();
        return next;
    }

    private static QuestSavedData root(ServerPlayer player) {
        DimensionDataStorage storage = player.serverLevel().getDataStorage();
        return storage.computeIfAbsent(new SavedData.Factory<>(QuestSavedData::new, QuestSavedData::load, null), DATA_ID);
    }

    public static final class QuestSavedData extends SavedData {
        private final Map<UUID, Integer> bonkCounts = new HashMap<>();

        static QuestSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
            QuestSavedData data = new QuestSavedData();
            CompoundTag bonks = tag.getCompound("Bonks");
            for (String key : bonks.getAllKeys()) {
                data.bonkCounts.put(UUID.fromString(key), bonks.getInt(key));
            }
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            CompoundTag bonks = new CompoundTag();
            bonkCounts.forEach((uuid, count) -> bonks.putInt(uuid.toString(), count));
            tag.put("Bonks", bonks);
            return tag;
        }
    }
}
