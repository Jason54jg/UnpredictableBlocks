package com.jason54.unpredictableblocks.luck;

/**
 * Valeur de chance d'un Lucky Block ou d'un joueur (-100 à +100).
 */
public record LuckStat(int value) {
    public static final int MIN = -100;
    public static final int MAX = 100;

    public LuckStat {
        value = clamp(value);
    }

    public static int clamp(int raw) {
        return Math.max(MIN, Math.min(MAX, raw));
    }

    public LuckStat add(int delta) {
        return new LuckStat(value + delta);
    }

    public LuckTier tier() {
        if (value >= 50) {
            return LuckTier.HIGH;
        }
        if (value <= -50) {
            return LuckTier.LOW;
        }
        return LuckTier.NEUTRAL;
    }

    /**
     * Interpolation linéaire pour les probabilités entre -100, 0 et +100.
     */
    public float highOutcomeWeight() {
        return normalizeToUnit(value);
    }

    public float lowOutcomeWeight() {
        return normalizeToUnit(-value);
    }

    public float neutralOutcomeWeight() {
        return 1.0f - Math.abs(normalizeToUnit(value));
    }

    private static float normalizeToUnit(int v) {
        return (clamp(v) + MAX) / 200.0f;
    }
}
