package com.jason54.unpredictableblocks.registry;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;

/**
 * Règles de jeu du mod, réglables à la création du monde (Plus d'options → Règles du jeu)
 * puis avec {@code /gamerule unpredictableblocks:<nom> <valeur>}.
 */
public final class ModGameRules {
    private static final DeferredRegister<GameRule<?>> RULES =
            DeferredRegister.create(UnpredictableBlocks.MOD_ID, Registries.GAME_RULE);

    /** Quantité de Lucky Blocks naturels en % de la normale : 0 = aucun, 100 = normal, 1000 = ×10. */
    public static final RegistrySupplier<GameRule<Integer>> LUCKY_DENSITY = RULES.register("lucky_density",
            () -> new GameRule<>(GameRuleCategory.SPAWNING, GameRuleType.INT, IntegerArgumentType.integer(0, 1000),
                    GameRuleTypeVisitor::visitInteger, Codec.intRange(0, 1000), value -> value, 100, FeatureFlagSet.of()));

    /** Socles / autels / sanctuaires autour des Lucky Blocks naturels. */
    public static final RegistrySupplier<GameRule<Boolean>> LUCKY_SHRINES = RULES.register("lucky_shrines",
            () -> new GameRule<>(GameRuleCategory.SPAWNING, GameRuleType.BOOL, BoolArgumentType.bool(),
                    GameRuleTypeVisitor::visitBoolean, Codec.BOOL, value -> value ? 1 : 0, true, FeatureFlagSet.of()));

    private ModGameRules() {}

    public static void register() {
        RULES.register();
    }

    /** Multiplicateur de densité (1.0 = normal). */
    public static double density(ServerLevel level) {
        return level.getGameRules().get(LUCKY_DENSITY.get()) / 100.0;
    }

    public static boolean shrines(ServerLevel level) {
        return level.getGameRules().get(LUCKY_SHRINES.get());
    }
}
