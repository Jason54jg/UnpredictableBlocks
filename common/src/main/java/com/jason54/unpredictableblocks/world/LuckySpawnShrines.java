package com.jason54.unpredictableblocks.world;

import com.jason54.unpredictableblocks.theme.LuckyRarity;
import com.jason54.unpredictableblocks.theme.LuckyVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Petites structures autour des Lucky Blocks naturels : socle (commun), autel (rare), sanctuaire (légendaire).
 * Palette changée pour les variantes dorée / maudite.
 * <p>
 * Tout est construit dans le chunk en cours de peuplement (pas de chargement de voisins, sinon blocage au
 * chargement du monde) : la structure est recentrée si besoin, puis aplanie (fondations dessous, dégagement dessus).
 */
public final class LuckySpawnShrines {
    private static final int MAX_SLOPE = 3;

    private LuckySpawnShrines() {}

    /** Chance qu'un Lucky Block naturel ait sa structure, selon sa rareté (monde normal). */
    public static double chance(LuckyRarity rarity, boolean denseWorld) {
        double base = switch (rarity) {
            case COMMON -> 0.7;
            case RARE -> 0.9;
            case LEGENDARY -> 1.0;
        };
        return denseWorld ? base * 0.35 : base;
    }

    private record Palette(Block floor, Block floorAlt, Block pedestal, Block pillar, Block light, Block accent) {}

    private static Palette palette(LuckyRarity rarity, LuckyVariant variant) {
        if (variant == LuckyVariant.CURSED) {
            return new Palette(Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS,
                    Blocks.CRYING_OBSIDIAN, Blocks.POLISHED_BLACKSTONE_WALL, Blocks.SOUL_LANTERN, Blocks.SOUL_SAND);
        }
        Palette base = switch (rarity) {
            case COMMON -> new Palette(Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE,
                    Blocks.POLISHED_ANDESITE, Blocks.COBBLESTONE_WALL, Blocks.TORCH, Blocks.MOSS_BLOCK);
            case RARE -> new Palette(Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS,
                    Blocks.CHISELED_STONE_BRICKS, Blocks.STONE_BRICK_WALL, Blocks.LANTERN, Blocks.MOSSY_STONE_BRICKS);
            case LEGENDARY -> new Palette(Blocks.SMOOTH_QUARTZ, Blocks.QUARTZ_BRICKS,
                    Blocks.GOLD_BLOCK, Blocks.QUARTZ_PILLAR, Blocks.END_ROD, Blocks.AMETHYST_CLUSTER);
        };
        if (variant == LuckyVariant.GOLDEN) {
            return new Palette(base.floor(), Blocks.RAW_GOLD_BLOCK, Blocks.GOLD_BLOCK, base.pillar(), base.light(), Blocks.GOLD_BLOCK);
        }
        return base;
    }

    /**
     * Construit la structure centrée sur {@code ground} (bloc de sol sous le futur Lucky Block).
     *
     * @return position où poser le Lucky Block, ou {@code null} si le terrain ne convient pas (rien n'est construit)
     */
    public static BlockPos build(ServerLevel level, LevelChunk chunk, BlockPos ground, LuckyRarity rarity,
                                 LuckyVariant variant, RandomSource random) {
        int radius = switch (rarity) {
            case COMMON -> 1;
            case RARE -> 2;
            case LEGENDARY -> 3;
        };
        // Recentre dans le chunk plutôt que d'abandonner près du bord
        int localX = Math.clamp(ground.getX() & 15, radius, 15 - radius);
        int localZ = Math.clamp(ground.getZ() & 15, radius, 15 - radius);
        int surfaceY = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, localX, localZ);
        ground = new BlockPos(chunk.getPos().getMinBlockX() + localX, surfaceY, chunk.getPos().getMinBlockZ() + localZ);
        if (!isBuildable(chunk, localX, localZ, radius, surfaceY)) {
            return null;
        }
        Palette p = palette(rarity, variant);
        int height = radius + 3;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos floor = ground.offset(dx, 0, dz);
                set(level, floor, (random.nextInt(4) == 0 ? p.floorAlt() : p.floor()).defaultBlockState());
                // Fondations jusqu'au sol (4 blocs max) pour ne pas flotter au-dessus d'un creux
                for (int down = 1; down <= 4 && !level.getBlockState(floor.below(down)).isSolidRender(); down++) {
                    set(level, floor.below(down), p.floor().defaultBlockState());
                }
                for (int up = 1; up <= height; up++) {
                    set(level, floor.above(up), Blocks.AIR.defaultBlockState());
                }
            }
        }

        BlockPos luckyPos = switch (rarity) {
            case COMMON -> {
                corners(level, ground.above(), 1, p.light().defaultBlockState());
                yield ground.above();
            }
            case RARE -> {
                set(level, ground.above(), p.pedestal().defaultBlockState());
                for (BlockPos corner : cornerPositions(ground.above(), 2)) {
                    set(level, corner, p.pillar().defaultBlockState());
                    set(level, corner.above(), p.light().defaultBlockState());
                }
                yield ground.above(2);
            }
            case LEGENDARY -> {
                set(level, ground.above(), p.pedestal().defaultBlockState());
                for (BlockPos side : new BlockPos[]{ground.above().north(), ground.above().south(),
                        ground.above().east(), ground.above().west()}) {
                    set(level, side, p.accent().defaultBlockState());
                }
                for (BlockPos corner : cornerPositions(ground.above(), 3)) {
                    for (int y = 0; y < 3; y++) {
                        set(level, corner.above(y), p.pillar().defaultBlockState());
                    }
                    set(level, corner.above(3), p.light().defaultBlockState());
                }
                yield ground.above(2);
            }
        };
        return luckyPos;
    }

    /** Sol naturel (pas d'eau, pas de tronc dans le volume) et dénivelé ≤ {@value #MAX_SLOPE} blocs sur l'emprise. */
    private static boolean isBuildable(LevelChunk chunk, int localX, int localZ, int radius, int centerY) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int y = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, localX + dx, localZ + dz);
                if (Math.abs(y - centerY) > MAX_SLOPE) {
                    return false;
                }
                BlockPos column = new BlockPos(chunk.getPos().getMinBlockX() + localX + dx, centerY,
                        chunk.getPos().getMinBlockZ() + localZ + dz);
                if (!chunk.getBlockState(column.atY(y)).getFluidState().isEmpty()) {
                    return false;
                }
                // Pas de tronc dans le volume dégagé (on couperait l'arbre en deux)
                for (int up = 0; up <= radius + 3; up++) {
                    if (chunk.getBlockState(column.above(up)).is(BlockTags.LOGS)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static BlockPos[] cornerPositions(BlockPos center, int radius) {
        return new BlockPos[]{center.offset(-radius, 0, -radius), center.offset(radius, 0, -radius),
                center.offset(-radius, 0, radius), center.offset(radius, 0, radius)};
    }

    private static void corners(ServerLevel level, BlockPos center, int radius, BlockState state) {
        for (BlockPos corner : cornerPositions(center, radius)) {
            set(level, corner, state);
        }
    }

    /** NOTIFY_CLIENTS seulement : pas de mise à jour des voisins pendant le peuplement. */
    private static void set(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 2);
    }
}
