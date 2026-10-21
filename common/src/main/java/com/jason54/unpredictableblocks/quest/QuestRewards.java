package com.jason54.unpredictableblocks.quest;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.util.ServerLevels;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class QuestRewards {
    private QuestRewards() {}

    public static void complete(ServerPlayer player, String advancementPath, String criterion) {
        Identifier id = Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, advancementPath);
        AdvancementHolder advancement = ServerLevels.of(player).getServer().getAdvancements().get(id);
        if (advancement != null) {
            player.getAdvancements().award(advancement, criterion);
            if (advancementPath.startsWith("quest/")) {
                QuestMilestones.checkAfterQuest(player);
            }
        }
    }

    public static void complete(ServerPlayer player, String advancementPath) {
        complete(player, advancementPath, "completed");
    }

    public static boolean isDone(ServerPlayer player, String advancementPath) {
        Identifier id = Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, advancementPath);
        AdvancementHolder advancement = ServerLevels.of(player).getServer().getAdvancements().get(id);
        if (advancement == null) {
            return false;
        }
        return player.getAdvancements().getOrStartProgress(advancement).isDone();
    }
}
