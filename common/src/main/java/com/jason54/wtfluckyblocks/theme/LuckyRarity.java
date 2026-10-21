package com.jason54.wtfluckyblocks.theme;

public enum LuckyRarity {
    COMMON(60),
    RARE(30),
    LEGENDARY(10);

    private final int defaultWeight;

    LuckyRarity(int defaultWeight) {
        this.defaultWeight = defaultWeight;
    }

    public int defaultWeight() {
        return defaultWeight;
    }
}
