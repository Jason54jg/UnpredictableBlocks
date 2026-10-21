package com.jason54.unpredictableblocks.registry;

import com.jason54.unpredictableblocks.UnpredictableBlocks;

/** Les advancements sont data-driven ; cette classe réserve les hooks runtime. */
public final class ModAdvancements {
    private ModAdvancements() {}

    public static void register() {
        UnpredictableBlocks.LOGGER.info("Chargement des quêtes (advancements) depuis data/{}/advancement/", UnpredictableBlocks.MOD_ID);
    }
}
