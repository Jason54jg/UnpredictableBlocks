package com.jason54.unpredictableblocks.quest;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class QuestRewards {
    private QuestRewards() {}

    public static void complete(ServerPlayer player, String advancementPath, String criterion) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, advancementPath);
        AdvancementHolder advancement = player.server.getAdvancements().get(id);
        if (advancement != null) {
            player.getAdvancements().award(advancement, criterion);
        }
    }

    public static void complete(ServerPlayer player, String advancementPath) {
        complete(player, advancementPath, "completed");
    }
}
