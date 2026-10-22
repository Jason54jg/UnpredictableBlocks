package com.jason54.unpredictableblocks.world;

import com.jason54.unpredictableblocks.block.entity.LuckyBlockEntity;
import com.jason54.unpredictableblocks.registry.ModBlocks;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Plateforme OneBlock + Lucky Block de départ. */
public final class OneBlockArenaBuilder {
    private OneBlockArenaBuilder() {}

    public static void buildIfNeeded(ServerLevel level, WorldModProfile.WorldProfileData profile, BlockPos center) {
        if (profile.mode() != WorldPlayMode.ONEBLOCK || profile.oneBlockBuilt()) {
            return;
        }
        int radius = switch (profile.oneBlockPlayers()) {
            case 2 -> 2;
            case 3, 4 -> 3;
            default -> 1;
        };
        clearPlatformVolume(level, center, radius + 2, 6);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos top = center.offset(dx, -1, dz);
                level.setBlock(top.below(), Blocks.BEDROCK.defaultBlockState(), 3);
                level.setBlock(top, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            }
        }
        // Lucky à côté du spawn (pas au-dessus de la tête).
        BlockPos luckyPos = center.offset(1, 0, 0);
        BlockState lucky = ModBlocks.get(LuckyTheme.BOULANGERIE).defaultBlockState();
        level.setBlock(luckyPos, lucky, 3);
        if (level.getBlockEntity(luckyPos) instanceof LuckyBlockEntity be) {
            be.setLuck(profile.defaultBlockLuck());
        }
        profile.setOneBlockBuilt(true);
        profile.setDirty();
    }

    /** Pieds du joueur sur l'herbe centrale (bloc en Y-1). */
    public static BlockPos spawnPos(BlockPos center) {
        return center;
    }

    public static void expandPlatform(ServerLevel level, BlockPos center, int extraRadius) {
        for (int dx = -extraRadius; dx <= extraRadius; dx++) {
            for (int dz = -extraRadius; dz <= extraRadius; dz++) {
                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                    continue;
                }
                BlockPos top = center.offset(dx, -1, dz);
                level.setBlock(top.below(), Blocks.BEDROCK.defaultBlockState(), 3);
                level.setBlock(top, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            }
        }
    }

    private static void clearPlatformVolume(ServerLevel level, BlockPos center, int radius, int height) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -3; dy <= height; dy++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(p);
                    if (!state.isAir() && !state.is(Blocks.BEDROCK)) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
}
