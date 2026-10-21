package com.jason54.wtfluckyblocks.luck;

import com.jason54.wtfluckyblocks.theme.LuckyTheme;
import net.minecraft.util.RandomSource;

/**
 * Résout quel pool d'événements déclencher selon la chance effective.
 */
public final class LuckResolver {
    private LuckResolver() {}

    public record ResolvedOutcome(LuckTier tier, String poolSuffix) {}

    public static ResolvedOutcome resolve(LuckStat blockLuck, LuckStat playerModifier, RandomSource random) {
        int effective = LuckStat.clamp(blockLuck.value() + playerModifier.value());
        LuckStat combined = new LuckStat(effective);

        float high = combined.highOutcomeWeight();
        float low = combined.lowOutcomeWeight();
        float neutral = combined.neutralOutcomeWeight();
        float total = high + low + neutral;
        float roll = random.nextFloat() * total;

        if (roll < high) {
            return new ResolvedOutcome(LuckTier.HIGH, "high");
        }
        roll -= high;
        if (roll < low) {
            return new ResolvedOutcome(LuckTier.LOW, "low");
        }
        return new ResolvedOutcome(LuckTier.NEUTRAL, "neutral");
    }

    public static String lootTablePath(LuckyTheme theme, ResolvedOutcome outcome) {
        return "gameplay/lucky/" + theme.id() + "/" + outcome.poolSuffix();
    }
}
