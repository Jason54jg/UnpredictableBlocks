package com.jason54.unpredictableblocks.quest;

import com.jason54.unpredictableblocks.config.ModConfig;
import com.jason54.unpredictableblocks.lucky.PlayerLuckyBreakStats;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import com.jason54.unpredictableblocks.util.ModMessages;
import net.minecraft.server.level.ServerPlayer;

/** 10 cassages + quête du thème → advancement theme_master/&lt;id&gt; (perfection). */
public final class ThemeMasterProgress {
    private ThemeMasterProgress() {}

    public static void check(ServerPlayer player, LuckyTheme theme) {
        int required = ModConfig.gameplay().themeMasterBreaksOrDefault();
        if (PlayerLuckyBreakStats.breaks(player, theme) < required) {
            return;
        }
        if (!QuestRewards.isDone(player, "quest/" + theme.id())) {
            return;
        }
        String path = "theme_master/" + theme.id();
        if (QuestRewards.isDone(player, path)) {
            return;
        }
        QuestRewards.complete(player, path, "master");
        player.sendSystemMessage(ModMessages.msg("theme_master", theme.id()));
        ThemeMasterMilestones.checkAfterMaster(player);
    }
}
