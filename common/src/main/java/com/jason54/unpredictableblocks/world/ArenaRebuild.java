package com.jason54.unpredictableblocks.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

/** Efface et regénère OneBlock / Parcours (options mises à jour). */
public final class ArenaRebuild {
    private ArenaRebuild() {}

    public static void rebuild(ServerLevel level, WorldModProfile.WorldProfileData profile) {
        BlockPos origin = WorldProfileApplier.arenaOrigin(profile, new BlockPos(0, WorldProfileApplier.ARENA_Y, 0));
        clearArena(level, profile, origin);
        profile.setOneBlockBuilt(false);
        profile.setParcoursBuilt(false);
        profile.setDirty();
        ParcoursWorldState.get(level).resetCoopProgress();
        ParcoursWorldState.get(level).setSessionOpen(true);
        WorldProfileApplier.apply(level, profile, origin);
    }

    private static void clearArena(ServerLevel level, WorldModProfile.WorldProfileData profile, BlockPos origin) {
        int y0 = origin.getY() - 5;
        int y1 = origin.getY() + 8;
        int x1 = origin.getX() + profile.modeSettings().pathLengthBlocks() + 8;
        int laneCount = profile.modeSettings().parcoursLayout() == WorldModeSettings.ParcoursLayout.COOP
                ? 1
                : Math.clamp(profile.oneBlockPlayers(), 1, 4);
        int zRadius = Math.max(8, laneCount * ParcoursArenaBuilder.LANE_SPACING / 2 + 6);
        for (int x = origin.getX() - 6; x <= x1; x++) {
            for (int z = origin.getZ() - zRadius; z <= origin.getZ() + zRadius; z++) {
                for (int y = y0; y <= y1; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (!level.getBlockState(p).isAir()) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
        if (profile.mode() == WorldPlayMode.ONEBLOCK) {
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    for (int y = y0; y <= y1; y++) {
                        BlockPos p = origin.offset(dx, y - origin.getY(), dz);
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
}
