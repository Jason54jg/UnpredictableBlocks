package com.jason54.unpredictableblocks.theme;

import net.minecraft.util.RandomSource;

/**
 * Variante d'un Lucky Block posé naturellement (indépendante du thème) : teinte, taille et règles de cassage.
 * Stockée dans le {@code LuckyBlockEntity}, transmise à la fusée et au dispatch.
 */
public enum LuckyVariant {
    NORMAL(0xFFFFFFFF, 1.0F, 0, 1, 1, 0.5F, 0),
    /** Chance +30, butin ×2, une variation garantie. */
    GOLDEN(0xFFFFD86B, 1.0F, 30, 2, 1, 1.0F, 4),
    /** Chance −30, butin ×2 (pour le meilleur ou le pire), deux variations garanties. */
    CURSED(0xFF8A6A9A, 1.0F, -30, 2, 2, 1.0F, 4),
    /** Bloc 1.4×, butin ×3, trois tirages de variation. */
    GIANT(0xFFFFFFFF, 1.4F, 0, 3, 3, 0.5F, 3);

    private final int color;
    private final float scale;
    private final int luckBonus;
    private final int lootRolls;
    private final int variationRolls;
    private final float variationChance;
    /** Chance (en %) qu'un Lucky Block naturel ait cette variante. */
    private final int spawnPercent;

    LuckyVariant(int color, float scale, int luckBonus, int lootRolls, int variationRolls, float variationChance, int spawnPercent) {
        this.color = color;
        this.scale = scale;
        this.luckBonus = luckBonus;
        this.lootRolls = lootRolls;
        this.variationRolls = variationRolls;
        this.variationChance = variationChance;
        this.spawnPercent = spawnPercent;
    }

    public int color() {
        return color;
    }

    public float scale() {
        return scale;
    }

    public int luckBonus() {
        return luckBonus;
    }

    public int lootRolls() {
        return lootRolls;
    }

    public int variationRolls() {
        return variationRolls;
    }

    public float variationChance() {
        return variationChance;
    }

    public static LuckyVariant byOrdinal(int ordinal) {
        LuckyVariant[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : NORMAL;
    }

    /** Variante d'un bloc naturel : ~89 % normal, 4 % doré, 4 % maudit, 3 % géant. */
    public static LuckyVariant rollNatural(RandomSource random) {
        int roll = random.nextInt(100);
        for (LuckyVariant variant : values()) {
            if (variant.spawnPercent == 0) {
                continue;
            }
            if (roll < variant.spawnPercent) {
                return variant;
            }
            roll -= variant.spawnPercent;
        }
        return NORMAL;
    }
}
