package com.jason54.unpredictableblocks;

import com.jason54.unpredictableblocks.config.ModConfig;
import com.jason54.unpredictableblocks.registry.ModAdvancements;
import com.jason54.unpredictableblocks.registry.ModBlockEntities;
import com.jason54.unpredictableblocks.registry.ModBlocks;
import com.jason54.unpredictableblocks.registry.ModItems;
import com.jason54.unpredictableblocks.registry.ModLoot;
import com.jason54.unpredictableblocks.registry.ModSounds;
import com.jason54.unpredictableblocks.world.LuckyBlockSpawnHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UnpredictableBlocks {
    public static final String MOD_ID = "unpredictableblocks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        ModConfig.load();
        ModBlocks.register();
        ModBlockEntities.register();
        ModItems.register();
        ModSounds.register();
        ModLoot.register();
        ModAdvancements.register();
        LuckyBlockSpawnHooks.register();
        LOGGER.info("UnpredictableBlocks initialisé (MC {}+)", ModConfig.get().meta().supportedVersions());
    }
}
