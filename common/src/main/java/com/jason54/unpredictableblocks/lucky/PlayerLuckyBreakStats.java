package com.jason54.unpredictableblocks.lucky;

import com.jason54.unpredictableblocks.config.ModConfig;
import com.jason54.unpredictableblocks.luck.LuckTier;
import com.jason54.unpredictableblocks.quest.PlayerQuestData;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import com.jason54.unpredictableblocks.util.ModMessages;
import net.minecraft.server.level.ServerPlayer;

/** Compteurs cassages / HIGH / LOW par thème + combo 3× même thème. */
public final class PlayerLuckyBreakStats {
    private PlayerLuckyBreakStats() {}

    public static void onBreak(ServerPlayer player, LuckyTheme theme, LuckTier tier, int effectiveLuck) {
        PlayerQuestData.incrementCounter(player, "break_" + theme.id());
        if (tier == LuckTier.HIGH) {
            PlayerQuestData.incrementCounter(player, "high_" + theme.id());
        } else if (tier == LuckTier.LOW) {
            PlayerQuestData.incrementCounter(player, "low_" + theme.id());
        }
        trackCombo(player, theme);
        NearMissHandler.tryAward(player, tier, effectiveLuck);
    }

    public static int breaks(ServerPlayer player, LuckyTheme theme) {
        return PlayerQuestData.getCounter(player, "break_" + theme.id());
    }

    public static int highs(ServerPlayer player, LuckyTheme theme) {
        return PlayerQuestData.getCounter(player, "high_" + theme.id());
    }

    public static int lows(ServerPlayer player, LuckyTheme theme) {
        return PlayerQuestData.getCounter(player, "low_" + theme.id());
    }

    private static void trackCombo(ServerPlayer player, LuckyTheme theme) {
        var gameplay = ModConfig.gameplay();
        int hits = gameplay.comboHitsRequiredOrDefault();
        PlayerQuestData.updateComboChain(player, theme.id(), gameplay.comboWindowTicksOrDefault(), chain -> {
            if (chain.count() == hits) {
                PlayerQuestData.grantComboPity(player, theme.id());
                player.sendSystemMessage(ModMessages.msg("combo_pity", hits, theme.id()));
            }
        });
    }
}
