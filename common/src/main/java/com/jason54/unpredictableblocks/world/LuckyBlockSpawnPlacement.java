package com.jason54.unpredictableblocks.world;

import com.jason54.unpredictableblocks.compat.McAccess;
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

    /** Mode « Plat Lucky » : un emplacement de grille tous les 8 blocs. */
    private static final int FLAT_GRID = 8;
    private static final double FLAT_GRID_CHANCE = 0.6;
    /** Mode « Lucky dense » : chance par essai ×4, essais ×2. */
    private static final double DENSE_CHANCE_FACTOR = 4.0;

    public static void populateChunk(ServerLevel level, LevelChunk chunk) {
        LuckyWorldMode mode = LuckyWorldMode.of(level);
        boolean overworld = level.dimension().equals(Level.OVERWORLD);
        if (overworld && mode == LuckyWorldMode.SKYBLOCK) {
            return; // Pas de sol : les Lucky Blocks viennent du générateur de l'île (LuckySkyblock)
        }
        RandomSource random = level.getRandom();
        if (overworld && mode == LuckyWorldMode.FLAT) {
            for (int localX = FLAT_GRID / 2; localX < 16; localX += FLAT_GRID) {
                for (int localZ = FLAT_GRID / 2; localZ < 16; localZ += FLAT_GRID) {
                    if (random.nextDouble() < FLAT_GRID_CHANCE) {
                        tryPlaceOnSurface(level, chunk, localX, localZ, random, 0.0);
                    }
                }
            }
            return;
        }
        ModConfig.Spawn spawn = ModConfig.get().spawn();
        double chance = spawn.chancePerAttemptOrDefault();
        int attempts = spawn.attemptsPerChunkOrDefault();
        if (mode == LuckyWorldMode.DENSE) {
            chance = Math.min(1.0, chance * DENSE_CHANCE_FACTOR);
            attempts *= 2;
        }
        boolean dense = mode == LuckyWorldMode.DENSE;
        for (int i = 0; i < attempts; i++) {
            if (random.nextDouble() < chance) {
                // Structures (socle / autel / sanctuaire) seulement dans l'overworld
                tryPlaceOnSurface(level, chunk, random.nextInt(16), random.nextInt(16), random, overworld ? (dense ? -1.0 : 1.0) : 0.0);
            }
        }
    }

    /**
     * @param shrineMode 0 = jamais de structure, 1 = chance normale, -1 = chance réduite (monde dense)
     */
    private static void tryPlaceOnSurface(ServerLevel level, LevelChunk chunk, int localX, int localZ, RandomSource random,
                                          double shrineMode) {
        int worldX = chunk.getPos().getMinBlockX() + localX;
        int worldZ = chunk.getPos().getMinBlockZ() + localZ;
        // Sol réel, feuillage ignoré : le Lucky Block se pose sous les arbres plutôt que sur leur cime.
        int surfaceY = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, localX, localZ);
        if (surfaceY <= level.getMinY() || surfaceY >= level.getMaxY()) {
            return;
        }
        BlockPos ground = new BlockPos(worldX, surfaceY, worldZ);
        BlockPos placePos = ground.above();
        BlockState at = chunk.getBlockState(placePos);
        BlockState below = chunk.getBlockState(ground);
        // Sol solide au sens collision (herbe, terre, pierre…) — isSolidRender rate trop de sols.
        if (!at.isAir() || !McAccess.blocksMotion(below)) {
            return;
        }
        LuckyTheme theme = pickTheme(random, level, placePos);
        if (theme == null) {
            return;
        }
        com.jason54.unpredictableblocks.theme.LuckyVariant variant = com.jason54.unpredictableblocks.theme.LuckyVariant.rollNatural(random);
        if (shrineMode != 0.0 && random.nextDouble() < LuckySpawnShrines.chance(theme.rarity(), shrineMode < 0)) {
            BlockPos shrineTop = LuckySpawnShrines.build(level, chunk, ground, theme.rarity(), variant, random);
            if (shrineTop != null) {
                placePos = shrineTop;
            }
        }
        place(level, placePos, theme, variant);
    }

    /** Pose un Lucky Block au thème tiré selon la config (rareté, biome, dimension). */
    public static boolean placeRandom(ServerLevel level, BlockPos placePos, RandomSource random) {
        LuckyTheme theme = pickTheme(random, level, placePos);
        if (theme == null) {
            return false;
        }
        return place(level, placePos, theme, com.jason54.unpredictableblocks.theme.LuckyVariant.rollNatural(random));
    }

    private static boolean place(ServerLevel level, BlockPos placePos, LuckyTheme theme,
                                 com.jason54.unpredictableblocks.theme.LuckyVariant variant) {
        BlockState state = ModBlocks.get(theme).defaultBlockState();
        // NOTIFY_CLIENTS seulement : pas de maj voisins (= pas de getChunk pendant le placement).
        if (!level.setBlock(placePos, state, 2)) {
            return false;
        }
        if (level.getBlockEntity(placePos) instanceof com.jason54.unpredictableblocks.block.entity.LuckyBlockEntity luckyBe) {
            luckyBe.setLuck(com.jason54.unpredictableblocks.config.ModConfig.get().meta().defaultBlockLuck());
            luckyBe.setVariant(variant);
        }
        return true;
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
            if (cfg.rarity() != rarity
                    || (cfg.nightOnly() && !night)
                    || !matchesBiome(cfg, biomePath)) {
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
        return ModConfig.themeSpawn(theme);
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
