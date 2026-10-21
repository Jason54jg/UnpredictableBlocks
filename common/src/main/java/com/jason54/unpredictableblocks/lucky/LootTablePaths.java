package com.jason54.unpredictableblocks.lucky;

import com.jason54.unpredictableblocks.config.ModConfig;
import com.jason54.unpredictableblocks.luck.LuckResolver;
import com.jason54.unpredictableblocks.luck.LuckTier;
import com.jason54.unpredictableblocks.theme.LuckyTheme;

/** Chemin loot table gameplay + overrides config (tier HIGH). */
public final class LootTablePaths {
    private LootTablePaths() {}

    public static String gameplayPath(LuckyTheme theme, LuckResolver.ResolvedOutcome outcome) {
        if (outcome.tier() == LuckTier.HIGH) {
            var overrides = ModConfig.get().lootHighOverrides();
            if (overrides != null) {
                String custom = overrides.get(theme.id());
                if (custom != null && !custom.isBlank()) {
                    return custom.startsWith("gameplay/") ? custom : "gameplay/lucky/" + theme.id() + "/" + custom;
                }
            }
        }
        return LuckResolver.lootTablePath(theme, outcome);
    }
}
