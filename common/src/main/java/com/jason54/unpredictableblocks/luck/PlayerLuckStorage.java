package com.jason54.unpredictableblocks.luck;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

/**
 * Modificateurs de chance du joueur (craft quêtes, items consommables).
 */
public final class PlayerLuckStorage {
    private static final String DATA_NAME = UnpredictableBlocks.MOD_ID + "_player_luck";
    private static final String TAG_MOD = "LuckModifier";

    private PlayerLuckStorage() {}

    public static LuckStat getModifier(ServerPlayer player) {
        return new LuckStat(get(player).modifier);
    }

    public static void addModifier(ServerPlayer player, int delta) {
        PlayerLuckData data = get(player);
        data.modifier = LuckStat.clamp(data.modifier + delta);
        data.setDirty();
    }

    private static PlayerLuckData get(ServerPlayer player) {
        DimensionDataStorage storage = player.serverLevel().getDataStorage();
        return storage.computeIfAbsent(new SavedData.Factory<>(PlayerLuckData::new, PlayerLuckData::load, null), DATA_NAME);
    }

    public static final class PlayerLuckData extends SavedData {
        int modifier;

        static PlayerLuckData load(CompoundTag tag, HolderLookup.Provider registries) {
            PlayerLuckData data = new PlayerLuckData();
            data.modifier = tag.getInt(TAG_MOD);
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putInt(TAG_MOD, modifier);
            return tag;
        }
    }

    public static boolean hasQuestBonus(Player player, LuckyTheme theme) {
        // Les advancements débloquent +100 via commande / critère custom — hook pour extensions.
        return false;
    }
}
