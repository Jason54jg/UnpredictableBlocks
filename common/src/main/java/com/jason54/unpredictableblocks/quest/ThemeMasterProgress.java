package com.jason54.unpredictableblocks.quest;

import com.jason54.unpredictableblocks.lucky.PlayerLuckyBreakStats;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** 10 cassages + quête du thème → advancement theme_master/&lt;id&gt; (perfection). */
public final class ThemeMasterProgress {
    private static final int BREAKS_REQUIRED = 10;

    private ThemeMasterProgress() {}

    public static void check(ServerPlayer player, LuckyTheme theme) {
        if (PlayerLuckyBreakStats.breaks(player, theme) < BREAKS_REQUIRED) {
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
        player.sendSystemMessage(Component.literal("§6Maître du thème §f" + theme.id() + "§6 !"));
    }
}
