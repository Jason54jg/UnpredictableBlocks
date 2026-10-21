package com.jason54.unpredictableblocks.luck;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.util.ServerLevels;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

/**
 * Modificateurs de chance du joueur (craft quêtes, items consommables).
 */
public final class PlayerLuckStorage {
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
        SavedDataStorage storage = ServerLevels.of(player).getDataStorage();
        return storage.computeIfAbsent(PlayerLuckData.TYPE);
    }

    public static final class PlayerLuckData extends SavedData {
        private static final Codec<PlayerLuckData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf(TAG_MOD).forGetter(data -> data.modifier)
        ).apply(instance, modifier -> {
            PlayerLuckData data = new PlayerLuckData();
            data.modifier = modifier;
            return data;
        }));

        public static final SavedDataType<PlayerLuckData> TYPE = new SavedDataType<>(
                Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, "player_luck"),
                PlayerLuckData::new,
                CODEC,
                DataFixTypes.SAVED_DATA_SCOREBOARD
        );

        int modifier;

        private PlayerLuckData() {
            modifier = 0;
        }
    }

    public static boolean hasQuestBonus(Player player, LuckyTheme theme) {
        // Les advancements débloquent +100 via commande / critère custom — hook pour extensions.
        return false;
    }
}
