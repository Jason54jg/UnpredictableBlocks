package com.jason54.unpredictableblocks.block;

import com.jason54.unpredictableblocks.compat.McAccess;

import net.minecraft.world.phys.Vec3;

import com.jason54.unpredictableblocks.config.ModConfig;
import com.jason54.unpredictableblocks.util.ModMessages;
import com.jason54.unpredictableblocks.event.LuckyBreakFeedback;
import com.jason54.unpredictableblocks.event.LuckyVariations;
import com.jason54.unpredictableblocks.event.ThemeEventEffects;
import com.jason54.unpredictableblocks.item.CollectionCardItem;
import com.jason54.unpredictableblocks.lucky.LootTablePaths;
import com.jason54.unpredictableblocks.luck.LuckResolver;
import com.jason54.unpredictableblocks.luck.LuckTier;
import com.jason54.unpredictableblocks.lucky.PlayerLuckyBreakStats;
import com.jason54.unpredictableblocks.quest.CardCollection;
import com.jason54.unpredictableblocks.quest.ThemeMasterProgress;
import com.jason54.unpredictableblocks.quest.JdrDiceRoll;
import com.jason54.unpredictableblocks.quest.PlayerQuestData;
import com.jason54.unpredictableblocks.structure.StructurePlacer;
import com.jason54.unpredictableblocks.world.ChunkLuckAura;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import com.jason54.unpredictableblocks.theme.LuckyVariant;
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
        dispatch(level, player, origin, theme, outcome, effectiveLuck, LuckyVariant.NORMAL);
    }

    public static void dispatch(
            ServerLevel level,
            ServerPlayer player,
            BlockPos origin,
            LuckyTheme theme,
            LuckResolver.ResolvedOutcome outcome,
            int effectiveLuck,
            LuckyVariant variant
    ) {
        runDispatch(level, player, origin, theme, outcome, effectiveLuck, variant);
    }

    private static void runDispatch(
            ServerLevel level,
            ServerPlayer player,
            BlockPos origin,
            LuckyTheme theme,
            LuckResolver.ResolvedOutcome outcome,
            int effectiveLuck,
            LuckyVariant variant
    ) {
        ChunkLuckAura.onLuckyBreak(level, origin, player);
        if (variant != LuckyVariant.NORMAL) {
            player.sendSystemMessage(ModMessages.msg("variant." + variant.name().toLowerCase(java.util.Locale.ROOT)));
        }

        LuckResolver.ResolvedOutcome effective = outcome;
        if (theme == LuckyTheme.JDR_D20) {
            LuckTier d20 = JdrDiceRoll.rollD20(player, level.getRandom());
            effective = new LuckResolver.ResolvedOutcome(d20, suffixFor(d20));
        }

        if (theme == LuckyTheme.SPEEDRUNNER) {
            PlayerQuestData.startSpeedrun(player);
            player.sendSystemMessage(ModMessages.msg("speedrun_start"));
        }

        if (theme == LuckyTheme.PLOMBERIE) {
            PlayerQuestData.startPlomberieWindow(player, 20L * 60 * 5);
            player.sendSystemMessage(ModMessages.msg("plomberie_start"));
        }

        PlayerQuestData.recordLuckOutcome(player, effective.tier());
        LuckyBreakFeedback.play(level, player, origin, effective.tier());

        float cardChance = (float) ModConfig.gameplay().cardDropOnHighOrDefault();
        if (effective.tier() == LuckTier.HIGH && cardChance > 0f && level.getRandom().nextFloat() < cardChance) {
            player.getInventory().add(CollectionCardItem.forTheme(theme));
            CardCollection.tryAwardCardAlbum(player);
        }

        Identifier table = Identifier.fromNamespaceAndPath(
                "unpredictableblocks",
                LootTablePaths.gameplayPath(theme, effective)
        );

        // CHEST : ORIGIN requis, THIS_ENTITY optionnel.
        // ALL_PARAMS (generic) exige ~11 clés → IllegalArgumentException → le bloc ne disparaît pas.
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(origin))
                .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                .create(LootContextParamSets.CHEST);

        var lootTable = level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, table));
        for (int roll = 0; roll < variant.lootRolls(); roll++) {
            for (var stack : lootTable.getRandomItems(params)) {
                McAccess.giveOrDrop(player, stack);
            }
        }

        StructurePlacer.tryPlaceForTheme(level, origin, theme, effective.tier());
        ThemeEventEffects.apply(level, player, origin, theme, effective.tier());
        LuckyVariations.roll(level, player, origin, theme, effective.tier(), variant.variationRolls(), variant.variationChance());

        PlayerLuckyBreakStats.onBreak(player, theme, effective.tier(), effectiveLuck);
        ThemeMasterProgress.check(player, theme);
        com.jason54.unpredictableblocks.world.ModeGameplayHandler.afterLuckyBreak(level, player, origin, theme, effective.tier());
    }

    private static String suffixFor(LuckTier tier) {
        return switch (tier) {
            case HIGH -> "high";
            case LOW -> "low";
            case NEUTRAL -> "neutral";
        };
    }
}
