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
        BlockPos spawn = center;
        int radius = switch (profile.oneBlockPlayers()) {
            case 2 -> 2;
            case 3, 4 -> 3;
            default -> 1;
        };
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos top = spawn.offset(dx, -1, dz);
                level.setBlock(top.below(), Blocks.BEDROCK.defaultBlockState(), 3);
                level.setBlock(top, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            }
        }
        BlockPos luckyPos = spawn.above();
        BlockState lucky = ModBlocks.get(LuckyTheme.BOULANGERIE).defaultBlockState();
        level.setBlock(luckyPos, lucky, 3);
        if (level.getBlockEntity(luckyPos) instanceof LuckyBlockEntity be) {
            be.setLuck(profile.defaultBlockLuck());
        }
        profile.setOneBlockBuilt(true);
        profile.setDirty();
    }
}
