package com.jason54.unpredictableblocks.world;

import com.jason54.unpredictableblocks.config.ModConfig;
import com.jason54.unpredictableblocks.registry.ModBlocks;
import com.jason54.unpredictableblocks.theme.LuckyRarity;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Placement naturel configurable (taux par chunk + pondération rareté/biome/dimension).
 */
public final class LuckyBlockSpawnPlacement {
    private LuckyBlockSpawnPlacement() {}

    public static void populateChunk(ServerLevel level, LevelChunk chunk) {
        ModConfig.Spawn spawn = ModConfig.get().spawn();
        RandomSource random = level.getRandom();
        for (int i = 0; i < spawn.attemptsPerChunk(); i++) {
            if (random.nextFloat() > 0.65f) {
                continue;
            }
            BlockPos sample = sampleSurfacePos(chunk, random);
            LuckyTheme theme = pickTheme(random, level, sample);
            if (theme == null) {
                continue;
            }
            BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, sample);
            BlockState state = ModBlocks.get(theme).defaultBlockState();
            if (level.getBlockState(surface).isAir() && level.getBlockState(surface.below()).isSolidRender()) {
                level.setBlock(surface, state, 3);
                if (level.getBlockEntity(surface) instanceof com.jason54.unpredictableblocks.block.entity.LuckyBlockEntity luckyBe) {
                    luckyBe.setLuck(com.jason54.unpredictableblocks.config.ModConfig.get().meta().defaultBlockLuck());
                }
            }
        }
    }

    private static BlockPos sampleSurfacePos(LevelChunk chunk, RandomSource random) {
        int x = chunk.getPos().getMinBlockX() + random.nextInt(16);
        int z = chunk.getPos().getMinBlockZ() + random.nextInt(16);
        return new BlockPos(x, 0, z);
    }

    private static LuckyTheme pickTheme(RandomSource random, ServerLevel level, BlockPos pos) {
        LuckyRarity rarity = rollRarity(random);
        String biomePath = level.getBiome(pos).unwrapKey()
                .map(key -> key.identifier().getPath())
                .orElse("");
        boolean night = level.getDefaultClockTime() % 24000 > 13000;
        double dimensionFactor = dimensionWeight(level, spawnConfig().dimensions());

        double totalWeight = 0;
        for (LuckyTheme theme : LuckyTheme.values()) {
            ModConfig.ThemeSpawn cfg = themeConfig(theme);
            if (cfg.rarity() != rarity) {
                continue;
            }
            if (cfg.nightOnly() && !night) {
                continue;
            }
            if (!matchesBiome(cfg, biomePath)) {
                continue;
            }
            totalWeight += themeWeight(cfg, level) * dimensionFactor;
        }
        if (totalWeight <= 0) {
            return fallbackTheme(rarity, random);
        }

        double roll = random.nextDouble() * totalWeight;
        for (LuckyTheme theme : LuckyTheme.values()) {
            ModConfig.ThemeSpawn cfg = themeConfig(theme);
            if (cfg.rarity() != rarity || (cfg.nightOnly() && !night) || !matchesBiome(cfg, biomePath)) {
                continue;
            }
            roll -= themeWeight(cfg, level) * dimensionFactor;
            if (roll <= 0) {
                return theme;
            }
        }
        return fallbackTheme(rarity, random);
    }

    private static LuckyTheme fallbackTheme(LuckyRarity rarity, RandomSource random) {
        LuckyTheme[] candidates = java.util.Arrays.stream(LuckyTheme.values())
                .filter(t -> t.rarity() == rarity)
                .toArray(LuckyTheme[]::new);
        if (candidates.length == 0) {
            return null;
        }
        return candidates[random.nextInt(candidates.length)];
    }

    private static double themeWeight(ModConfig.ThemeSpawn cfg, ServerLevel level) {
        ResourceKey<Level> dim = level.dimension();
        if (dim.equals(Level.NETHER)) {
            return cfg.netherWeight();
        }
        if (dim.equals(Level.END)) {
            return cfg.endWeight();
        }
        return cfg.overworldWeight();
    }

    private static double dimensionWeight(ServerLevel level, ModConfig.DimensionWeights weights) {
        ResourceKey<Level> dim = level.dimension();
        if (dim.equals(Level.NETHER)) {
            return weights.nether();
        }
        if (dim.equals(Level.END)) {
            return weights.end();
        }
        return weights.overworld();
    }

    private static boolean matchesBiome(ModConfig.ThemeSpawn cfg, String biomePath) {
        for (String preferred : cfg.preferredBiomes()) {
            if ("*".equals(preferred) || biomePath.contains(preferred)) {
                return true;
            }
        }
        return false;
    }

    private static ModConfig.ThemeSpawn themeConfig(LuckyTheme theme) {
        var map = ModConfig.get().themes();
        ModConfig.ThemeSpawn fromFile = map.get(theme.id());
        return fromFile != null ? fromFile : ModConfig.ThemeSpawn.forTheme(theme);
    }

    private static ModConfig.Spawn spawnConfig() {
        return ModConfig.get().spawn();
    }

    private static LuckyRarity rollRarity(RandomSource random) {
        var weights = spawnConfig().rarityWeights();
        int total = weights.values().stream().mapToInt(Integer::intValue).sum();
        int roll = random.nextInt(Math.max(total, 1));
        for (var entry : weights.entrySet()) {
            roll -= entry.getValue();
            if (roll < 0) {
                return entry.getKey();
            }
        }
        return LuckyRarity.COMMON;
    }
}
