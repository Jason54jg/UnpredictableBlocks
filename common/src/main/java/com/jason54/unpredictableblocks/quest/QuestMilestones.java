package com.jason54.unpredictableblocks.quest;

import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.server.level.ServerPlayer;

/** Récompenses paliers 8 / 16 / 24 quêtes thématiques complétées. */
public final class QuestMilestones {
    private QuestMilestones() {}

    public static int countCompleted(ServerPlayer player) {
        int done = 0;
        for (LuckyTheme theme : LuckyTheme.values()) {
            if (QuestRewards.isDone(player, "quest/" + theme.id())) {
                done++;
            }
        }
        return done;
    }

    public static void checkAfterQuest(ServerPlayer player) {
        int done = countCompleted(player);
        if (done >= 8) {
            QuestRewards.complete(player, "milestone/quest_8", "tier");
        }
        if (done >= 16) {
            QuestRewards.complete(player, "milestone/quest_16", "tier");
        }
        if (done >= 24) {
            QuestRewards.complete(player, "milestone/quest_24", "tier");
        }
        if (done >= LuckyTheme.values().length) {
            QuestRewards.complete(player, "milestone/quest_32", "tier");
        }
    }
}
