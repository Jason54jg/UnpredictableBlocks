package com.jason54.unpredictableblocks.lucky;

import com.jason54.unpredictableblocks.luck.LuckTier;
import com.jason54.unpredictableblocks.quest.PlayerQuestData;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Compteurs cassages / HIGH / LOW par thème + combo 3× même thème. */
public final class PlayerLuckyBreakStats {
    private static final long COMBO_WINDOW_TICKS = 20L * 60 * 2;

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
        PlayerQuestData.updateComboChain(player, theme.id(), COMBO_WINDOW_TICKS, chain -> {
            if (chain.count() == 3) {
                PlayerQuestData.grantComboPity(player, theme.id());
                player.sendSystemMessage(Component.literal(
                        "§6Combo ×3 sur §f" + theme.id() + "§6 — prochain cassage : tier minimum §eNEUTRAL§6."
                ));
            }
        });
    }
}
