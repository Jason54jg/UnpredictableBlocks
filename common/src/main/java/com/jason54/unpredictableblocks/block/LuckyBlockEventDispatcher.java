package com.jason54.unpredictableblocks.block;

import com.jason54.unpredictableblocks.event.ThemeEventEffects;
import com.jason54.unpredictableblocks.luck.LuckResolver;
import com.jason54.unpredictableblocks.luck.LuckTier;
import com.jason54.unpredictableblocks.quest.CasinoJackpot;
import com.jason54.unpredictableblocks.quest.JdrDiceRoll;
import com.jason54.unpredictableblocks.quest.PlayerQuestData;
import com.jason54.unpredictableblocks.structure.StructurePlacer;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
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
        LuckResolver.ResolvedOutcome effective = outcome;
        if (theme == LuckyTheme.JDR_D20) {
            LuckTier d20 = JdrDiceRoll.rollD20(player, level.getRandom());
            effective = new LuckResolver.ResolvedOutcome(d20, suffixFor(d20));
        }

        if (theme == LuckyTheme.CASINO) {
            CasinoJackpot.spin(level, player, level.getRandom());
        }

        if (effective.tier() == LuckTier.LOW) {
            PlayerQuestData.setMissedReroll(player, theme);
        }

        ResourceLocation table = ResourceLocation.fromNamespaceAndPath(
                "unpredictableblocks",
                LuckResolver.lootTablePath(theme, effective)
        );

        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, origin.getCenter())
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withParameter(LootContextParams.BLOCK_STATE, level.getBlockState(origin))
                .create(LootContextParamSets.ALL_PARAMS);

        level.getServer().getLootTables().getLootTable(table).getRandomItems(params, player.getInventory()::placeItemBackInInventory);

        StructurePlacer.tryPlaceForTheme(level, origin, theme, effective.tier());
        ThemeEventEffects.apply(level, player, origin, theme, effective.tier());
    }

    private static String suffixFor(LuckTier tier) {
        return switch (tier) {
            case HIGH -> "high";
            case LOW -> "low";
            case NEUTRAL -> "neutral";
        };
    }
}
