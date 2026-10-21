package com.jason54.unpredictableblocks.block;

import com.jason54.unpredictableblocks.config.ModConfig;
import com.jason54.unpredictableblocks.event.LuckyBreakFeedback;
import com.jason54.unpredictableblocks.event.ThemeEventEffects;
import com.jason54.unpredictableblocks.item.CollectionCardItem;
import com.jason54.unpredictableblocks.lucky.LootTablePaths;
import com.jason54.unpredictableblocks.luck.LuckResolver;
import com.jason54.unpredictableblocks.luck.LuckTier;
import com.jason54.unpredictableblocks.lucky.PlayerLuckyBreakStats;
import com.jason54.unpredictableblocks.quest.CardCollection;
import com.jason54.unpredictableblocks.quest.ThemeMasterProgress;
import com.jason54.unpredictableblocks.quest.CasinoJackpot;
import com.jason54.unpredictableblocks.quest.JdrDiceRoll;
import com.jason54.unpredictableblocks.quest.PlayerQuestData;
import com.jason54.unpredictableblocks.structure.StructurePlacer;
import com.jason54.unpredictableblocks.world.ChunkLuckAura;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * Déclenche loot tables, structures et scripts d'événements pour le {@link LuckyTheme}
 * du bloc cassé (spécification fixe). Le paramètre {@code outcome} porte l'aléatoire tier.
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
        dispatch(level, player, origin, theme, outcome, 0);
    }

    public static void dispatch(
            ServerLevel level,
            ServerPlayer player,
            BlockPos origin,
            LuckyTheme theme,
            LuckResolver.ResolvedOutcome outcome,
            int effectiveLuck
    ) {
        runDispatch(level, player, origin, theme, outcome, effectiveLuck);
    }

    private static void runDispatch(
            ServerLevel level,
            ServerPlayer player,
            BlockPos origin,
            LuckyTheme theme,
            LuckResolver.ResolvedOutcome outcome,
            int effectiveLuck
    ) {
        ChunkLuckAura.onLuckyBreak(level, origin, player);

        LuckResolver.ResolvedOutcome effective = outcome;
        if (theme == LuckyTheme.JDR_D20) {
            LuckTier d20 = JdrDiceRoll.rollD20(player, level.getRandom());
            effective = new LuckResolver.ResolvedOutcome(d20, suffixFor(d20));
        }

        if (theme == LuckyTheme.CASINO) {
            CasinoJackpot.spin(level, player, level.getRandom());
        }

        if (theme == LuckyTheme.SPEEDRUNNER) {
            PlayerQuestData.startSpeedrun(player);
            player.sendSystemMessage(Component.literal("§eChrono lancé — atteignez l'End en moins de 3 minutes !"));
        }

        if (theme == LuckyTheme.PLOMBERIE) {
            PlayerQuestData.startPlomberieWindow(player, 20L * 60 * 5);
            player.sendSystemMessage(Component.literal("§6Fuite détectée — ouvrez un coffre avant 5 minutes !"));
        }

        PlayerQuestData.recordLuckOutcome(player, effective.tier());
        LuckyBreakFeedback.play(level, player, origin, effective.tier());

        if (effective.tier() == LuckTier.LOW) {
            PlayerQuestData.setMissedReroll(player, theme);
            long vipSeconds = ModConfig.gameplay().missedRerollTicksOrDefault() / 20L;
            player.sendSystemMessage(Component.literal(
                    "§cMalus enregistré — Jeton VIP utilisable (" + vipSeconds + "s) pour relancer « "
                            + theme.id() + " »."
            ));
        }

        float cardChance = (float) ModConfig.gameplay().cardDropOnHighOrDefault();
        if (effective.tier() == LuckTier.HIGH && cardChance > 0f && level.getRandom().nextFloat() < cardChance) {
            player.getInventory().add(CollectionCardItem.forTheme(theme));
            CardCollection.tryAwardCardAlbum(player);
        }

        Identifier table = Identifier.fromNamespaceAndPath(
                "unpredictableblocks",
                LootTablePaths.gameplayPath(theme, effective)
        );

        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, origin.getCenter())
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withParameter(LootContextParams.BLOCK_STATE, level.getBlockState(origin))
                .create(LootContextParamSets.ALL_PARAMS);

        level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, table))
                .getRandomItems(params, player.getInventory()::placeItemBackInInventory);

        StructurePlacer.tryPlaceForTheme(level, origin, theme, effective.tier());
        ThemeEventEffects.apply(level, player, origin, theme, effective.tier());

        PlayerLuckyBreakStats.onBreak(player, theme, effective.tier(), effectiveLuck);
        ThemeMasterProgress.check(player, theme);
    }

    private static String suffixFor(LuckTier tier) {
        return switch (tier) {
            case HIGH -> "high";
            case LOW -> "low";
            case NEUTRAL -> "neutral";
        };
    }
}
