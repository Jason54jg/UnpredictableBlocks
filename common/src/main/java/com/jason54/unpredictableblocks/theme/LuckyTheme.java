package com.jason54.unpredictableblocks.theme;

import net.minecraft.resources.ResourceLocation;
import com.jason54.unpredictableblocks.UnpredictableBlocks;

/**
 * Catalogue des 24 thèmes Lucky Block intégrés.
 */
public enum LuckyTheme {
    ANACHRONISME("anachronisme", LuckyRarity.RARE),
    BOULANGERIE("boulangerie", LuckyRarity.COMMON),
    BSOD("bsod", LuckyRarity.LEGENDARY),
    VOLAILLE("volaille", LuckyRarity.COMMON),
    IMPOTS("impots", LuckyRarity.RARE),
    STREAMER("streamer", LuckyRarity.RARE),
    PROPRETE("proprete", LuckyRarity.COMMON),
    GRAVITE_ZERO("gravite_zero", LuckyRarity.LEGENDARY),
    FAST_FOOD("fast_food", LuckyRarity.COMMON),
    CAUCHEMAR_MATIN("cauchemar_matin", LuckyRarity.RARE),
    FILM_ACTION("film_action", LuckyRarity.RARE),
    JDR_D20("jdr_d20", LuckyRarity.RARE),
    PENURIE_2020("penurie_2020", LuckyRarity.COMMON),
    COMPLOT("complot", LuckyRarity.LEGENDARY),
    LAG_BUG("lag_bug", LuckyRarity.LEGENDARY),
    DISCO_80("disco_80", LuckyRarity.RARE),
    PLOMBERIE("plomberie", LuckyRarity.COMMON),
    FORTUNE_COOKIE("fortune_cookie", LuckyRarity.RARE),
    CAMPING("camping", LuckyRarity.COMMON),
    GYM_BRO("gym_bro", LuckyRarity.RARE),
    ART_MODERNE("art_moderne", LuckyRarity.RARE),
    MAGIE_COMPTOIR("magie_comptoir", LuckyRarity.COMMON),
    CASINO("casino", LuckyRarity.LEGENDARY),
    INVASION_MEMES("invasion_memes", LuckyRarity.LEGENDARY);

    private final String id;
    private final LuckyRarity rarity;

    LuckyTheme(String id, LuckyRarity rarity) {
        this.id = id;
        this.rarity = rarity;
    }

    public String id() {
        return id;
    }

    public LuckyRarity rarity() {
        return rarity;
    }

    public ResourceLocation blockId() {
        return ResourceLocation.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, "lucky_" + id);
    }

    public ResourceLocation questId() {
        return ResourceLocation.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, "quest/" + id);
    }
}
