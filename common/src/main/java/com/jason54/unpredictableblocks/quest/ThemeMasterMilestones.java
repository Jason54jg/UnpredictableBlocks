package com.jason54.unpredictableblocks.quest;

import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.server.level.ServerPlayer;

/** Palier : 32 advancements theme_master complétés. */
public final class ThemeMasterMilestones {
    private ThemeMasterMilestones() {}

    public static int countMasters(ServerPlayer player) {
        int done = 0;
        for (LuckyTheme theme : LuckyTheme.values()) {
            if (QuestRewards.isDone(player, "theme_master/" + theme.id())) {
                done++;
            }
        }
        return done;
    }

    public static void checkAfterMaster(ServerPlayer player) {
        if (countMasters(player) >= LuckyTheme.values().length) {
            QuestRewards.complete(player, "milestone/theme_master_32", "all_masters");
        }
    }
}
