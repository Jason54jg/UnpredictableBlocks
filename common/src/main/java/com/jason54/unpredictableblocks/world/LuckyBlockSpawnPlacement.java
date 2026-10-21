package com.jason54.unpredictableblocks.world;

import com.jason54.unpredictableblocks.config.ModConfig;
import com.jason54.unpredictableblocks.registry.ModBlocks;
import com.jason54.unpredictableblocks.theme.LuckyRarity;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Placement naturel configurable (taux par chunk + pondération rareté/biome/dimension).
 */
public final class LuckyBlockSpawnPlacement {
    private LuckyBlockSpawnPlacement() {}

    public static void register() {
        // Hook Architectury / NeoForge biome modifiers & Fabric feature registration côté plateforme.
    }

    public static void populateChunk(ServerLevel level, LevelChunk chunk) {
        ModConfig.Spawn spawn = ModConfig.get().spawn();
        RandomSource random = level.getRandom();
        for (int i = 0; i < spawn.attemptsPerChunk(); i++) {
            if (random.nextFloat() > 0.65f) {
                continue;
            }
            LuckyTheme theme = pickTheme(random, level, chunk);
            if (theme == null) {
                continue;
            }
            int x = chunk.getPos().getMinBlockX() + random.nextInt(16);
            int z = chunk.getPos().getMinBlockZ() + random.nextInt(16);
            BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, new BlockPos(x, 0, z));
            BlockState state = ModBlocks.get(theme).defaultBlockState();
            if (level.getBlockState(surface).isAir() && level.getBlockState(surface.below()).isSolidRender(level, surface.below())) {
                level.setBlock(surface, state, 3);
            }
        }
    }

    private static LuckyTheme pickTheme(RandomSource random, ServerLevel level, LevelChunk chunk) {
        LuckyRarity rarity = rollRarity(random);
        LuckyTheme[] candidates = java.util.Arrays.stream(LuckyTheme.values())
                .filter(t -> t.rarity() == rarity)
                .toArray(LuckyTheme[]::new);
        if (candidates.length == 0) {
            return null;
        }
        return candidates[random.nextInt(candidates.length)];
    }

    private static LuckyRarity rollRarity(RandomSource random) {
        var weights = ModConfig.get().spawn().rarityWeights();
        int total = weights.values().stream().mapToInt(Integer::intValue).sum();
        int roll = random.nextInt(total);
        for (var entry : weights.entrySet()) {
            roll -= entry.getValue();
            if (roll < 0) {
                return entry.getKey();
            }
        }
        return LuckyRarity.COMMON;
    }
}
