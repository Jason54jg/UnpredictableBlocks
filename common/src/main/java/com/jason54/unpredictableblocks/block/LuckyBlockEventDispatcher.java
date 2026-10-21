package com.jason54.unpredictableblocks.block;

import com.jason54.unpredictableblocks.event.ThemeEventEffects;
import com.jason54.unpredictableblocks.luck.LuckResolver;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import com.jason54.unpredictableblocks.structure.StructurePlacer;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * Déclenche loot tables, structures et scripts d'événements par thème.
 */
public final class LuckyBlockEventDispatcher {
    private LuckyBlockEventDispatcher() {}

    public static void dispatch(
            ServerLevel level,
            ServerPlayer player,
            BlockPos origin,
            LuckyTheme theme,
            LuckResolver.ResolvedOutcome outcome
    ) {
        ResourceLocation table = ResourceLocation.fromNamespaceAndPath(
                "unpredictableblocks",
                LuckResolver.lootTablePath(theme, outcome)
        );

        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, origin.getCenter())
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withParameter(LootContextParams.BLOCK_STATE, level.getBlockState(origin))
                .create(LootContextParamSets.ALL_PARAMS);

        level.getServer().getLootTables().getLootTable(table).getRandomItems(params, player.getInventory()::placeItemBackInInventory);

        StructurePlacer.tryPlaceForTheme(level, origin, theme, outcome.tier());
        ThemeEventEffects.apply(level, player, origin, theme, outcome.tier());
    }
}
