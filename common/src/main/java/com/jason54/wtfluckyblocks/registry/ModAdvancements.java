package com.jason54.wtfluckyblocks.registry;

import com.jason54.wtfluckyblocks.WtfLuckyBlocks;

/** Les advancements sont data-driven ; cette classe réserve les hooks runtime. */
public final class ModAdvancements {
    private ModAdvancements() {}

    public static void register() {
        WtfLuckyBlocks.LOGGER.info("Chargement des quêtes (advancements) depuis data/{}/advancement/", WtfLuckyBlocks.MOD_ID);
    }
}
