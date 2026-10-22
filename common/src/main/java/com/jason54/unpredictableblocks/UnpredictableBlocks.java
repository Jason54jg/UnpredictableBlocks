package com.jason54.unpredictableblocks;

import com.jason54.unpredictableblocks.client.UnpredictableBlocksClient;
import com.jason54.unpredictableblocks.config.ModConfig;
import com.jason54.unpredictableblocks.registry.ModAdvancements;
import com.jason54.unpredictableblocks.registry.ModBlockEntities;
import com.jason54.unpredictableblocks.registry.ModBlocks;
import com.jason54.unpredictableblocks.registry.ModEntities;
import com.jason54.unpredictableblocks.registry.ModGameRules;
import com.jason54.unpredictableblocks.registry.ModItems;
import com.jason54.unpredictableblocks.registry.ModLoot;
import com.jason54.unpredictableblocks.registry.ModCreativeTabs;
import com.jason54.unpredictableblocks.registry.ModSounds;
import com.jason54.unpredictableblocks.command.CommandRegistration;
import com.jason54.unpredictableblocks.event.GearEffectHandler;
import com.jason54.unpredictableblocks.event.LuckyBlockAmbientHandler;
import com.jason54.unpredictableblocks.event.LuckHudHandler;
import com.jason54.unpredictableblocks.network.ModNetworking;
import com.jason54.unpredictableblocks.quest.FortuneCookieHandler;
import com.jason54.unpredictableblocks.quest.QuestTriggers;
import com.jason54.unpredictableblocks.world.LuckyBlockSpawnHooks;
import com.jason54.unpredictableblocks.world.LuckySkyblock;
import com.jason54.unpredictableblocks.world.ModeGameplayHandler;
import com.jason54.unpredictableblocks.world.OneBlockArenaBuilder;
import com.jason54.unpredictableblocks.world.WorldSetupHandler;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UnpredictableBlocks {
    public static final String MOD_ID = "unpredictableblocks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        ModConfig.load();
        ModBlocks.register();
        ModBlockEntities.register();
        ModEntities.register();
        ModGameRules.register();
        ModSounds.register();
        ModCreativeTabs.register();
        ModItems.register();
        ModNetworking.register();
        ModLoot.register();
        ModAdvancements.register();
        LuckyBlockSpawnHooks.register();
        LuckySkyblock.register();
        OneBlockArenaBuilder.register();
        WorldSetupHandler.register();
        ModeGameplayHandler.register();
        QuestTriggers.register();
        FortuneCookieHandler.register();
        GearEffectHandler.register();
        LuckHudHandler.register();
        LuckyBlockAmbientHandler.register();
        CommandRegistration.register();
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> UnpredictableBlocksClient.init());
        LOGGER.info("UnpredictableBlocks initialisé (MC {}+)", ModConfig.get().meta().supportedVersions());
    }
}
