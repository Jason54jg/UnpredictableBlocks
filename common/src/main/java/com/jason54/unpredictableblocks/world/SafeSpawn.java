package com.jason54.unpredictableblocks.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/** Spawn / respawn hors sol (maps normales et variations pieds). */
public final class SafeSpawn {
    private SafeSpawn() {}

    public static boolean isFeetSafe(ServerLevel level, BlockPos feet) {
        BlockState at = level.getBlockState(feet);
        BlockState head = level.getBlockState(feet.above());
        BlockState below = level.getBlockState(feet.below());
        if (!at.getCollisionShape(level, feet).isEmpty() || !head.getCollisionShape(level, feet.above()).isEmpty()) {
            return false;
        }
        if (!level.getFluidState(feet).isEmpty() || !level.getFluidState(feet.above()).isEmpty()) {
            return false;
        }
        return !below.getCollisionShape(level, feet.below()).isEmpty();
    }

    /** Surface MOTION_BLOCKING autour de (x,z), sinon scan vertical. */
    public static BlockPos findSurface(ServerLevel level, int x, int z) {
        level.getChunkSource().getChunk(x >> 4, z >> 4, true);
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos candidate = new BlockPos(x, Math.max(level.getMinY() + 1, surfaceY), z);
        if (isFeetSafe(level, candidate)) {
            return candidate;
        }
        int minY = level.getMinY() + 1;
        int maxY = level.getMaxY() - 2;
        for (int y = Math.min(maxY, surfaceY + 8); y >= minY; y--) {
            BlockPos feet = new BlockPos(x, y, z);
            if (isFeetSafe(level, feet)) {
                return feet;
            }
        }
        for (int y = surfaceY; y <= maxY; y++) {
            BlockPos feet = new BlockPos(x, y, z);
            if (isFeetSafe(level, feet)) {
                return feet;
            }
        }
        return candidate;
    }

    public static BlockPos findNear(ServerLevel level, BlockPos approx) {
        BlockPos best = findSurface(level, approx.getX(), approx.getZ());
        if (isFeetSafe(level, best)) {
            return best;
        }
        // Rayon limité : un scan large force trop de chunks (freeze NeoForge au join).
        for (int r = 1; r <= 3; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) {
                        continue;
                    }
                    BlockPos p = findSurface(level, approx.getX() + dx, approx.getZ() + dz);
                    if (isFeetSafe(level, p)) {
                        return p;
                    }
                }
            }
        }
        return best;
    }

    /** Si le joueur est enterré (map normale), le remonte à la surface. */
    public static void rescueIfBuried(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        BlockPos feet = player.blockPosition();
        if (isFeetSafe(level, feet)) {
            return;
        }
        BlockPos safe = findNear(level, feet);
        player.teleportTo(safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
    }
}