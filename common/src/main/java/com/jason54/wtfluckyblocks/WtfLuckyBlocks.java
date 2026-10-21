package com.jason54.wtfluckyblocks;

import com.jason54.wtfluckyblocks.config.ModConfig;
import com.jason54.wtfluckyblocks.registry.ModAdvancements;
import com.jason54.wtfluckyblocks.registry.ModBlockEntities;
import com.jason54.wtfluckyblocks.registry.ModBlocks;
import com.jason54.wtfluckyblocks.registry.ModItems;
import com.jason54.wtfluckyblocks.registry.ModLoot;
import com.jason54.wtfluckyblocks.registry.ModSounds;
import com.jason54.wtfluckyblocks.world.LuckyBlockSpawnPlacement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WtfLuckyBlocks {
    public static final String MOD_ID = "wtf_luckyblocks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        ModConfig.load();
        ModBlocks.register();
        ModBlockEntities.register();
        ModItems.register();
        ModSounds.register();
        ModLoot.register();
        ModAdvancements.register();
        LuckyBlockSpawnPlacement.register();
        LOGGER.info("WTF Lucky Blocks initialisé (MC {}+)", ModConfig.get().meta().supportedVersions());
    }
}
