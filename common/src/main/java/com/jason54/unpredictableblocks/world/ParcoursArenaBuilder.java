package com.jason54.unpredictableblocks.world;

import com.jason54.unpredictableblocks.block.entity.LuckyBlockEntity;
import com.jason54.unpredictableblocks.registry.ModBlocks;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Parcours Lucky Block : une piste par joueur (1–4), générée dans le void. */
public final class ParcoursArenaBuilder {
    public static final int PATH_LENGTH = 52;
    public static final int LANE_SPACING = 12;
    public static final int FLOOR_Y_OFFSET = 0;

    private ParcoursArenaBuilder() {}

    public static void buildIfNeeded(ServerLevel level, WorldModProfile.WorldProfileData profile, BlockPos arenaOrigin) {
        if (profile.mode() != WorldPlayMode.PARCOURS || profile.parcoursBuilt()) {
            return;
        }
        int lanes = Math.clamp(profile.oneBlockPlayers(), 1, 4);
        RandomSource random = level.getRandom();
        for (int lane = 0; lane < lanes; lane++) {
            buildLane(level, arenaOrigin, lane, lanes, profile.defaultBlockLuck(), random);
        }
        profile.setParcoursBuilt(true);
        profile.setDirty();
    }

    /** Position de spawn (pieds du joueur) au départ de la piste. */
    public static BlockPos laneSpawnPos(BlockPos arenaOrigin, int laneIndex, int laneCount) {
        BlockPos start = laneOriginAt(arenaOrigin, laneIndex, laneCount);
        return start.offset(0, 1, 0);
    }

    public static int laneForPlayer(java.util.UUID playerId, int laneCount) {
        return Math.floorMod(playerId.getLeastSignificantBits(), laneCount);
    }

    private static void buildLane(
            ServerLevel level,
            BlockPos origin,
            int laneIndex,
            int laneCount,
            int defaultLuck,
            RandomSource random
    ) {
        BlockPos laneOrigin = laneOriginAt(origin, laneIndex, laneCount);
        int baseY = origin.getY() + FLOOR_Y_OFFSET;
        BlockState path = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState accent = Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
        BlockState rail = Blocks.OAK_FENCE.defaultBlockState();

        for (int step = -2; step <= PATH_LENGTH; step++) {
            BlockPos foot = laneOrigin.offset(step, 0, 0);
            level.setBlock(foot.below(2), Blocks.BEDROCK.defaultBlockState(), 3);
            if (isGap(step)) {
                if (step == 18 || step == 36) {
                    level.setBlock(foot.below(), Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3);
                }
                continue;
            }
            level.setBlock(foot.below(), path, 3);
            if (step % 6 == 0) {
                level.setBlock(foot, accent, 3);
            }
            if (step >= 0 && step % 5 == 2) {
                placeLucky(level, foot.above(), pickTheme(laneIndex, step, random), defaultLuck, random);
            }
            level.setBlock(foot.offset(0, 1, -1), rail, 3);
            level.setBlock(foot.offset(0, 1, 1), rail, 3);
        }

        BlockPos finish = laneOrigin.offset(PATH_LENGTH + 1, 0, 0);
        for (int dx = 0; dx <= 2; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos p = finish.offset(dx, -1, dz);
                level.setBlock(p.below(), Blocks.BEDROCK.defaultBlockState(), 3);
                level.setBlock(p, dx == 1 && dz == 0 ? Blocks.GOLD_BLOCK.defaultBlockState() : path, 3);
            }
        }
        placeLucky(level, finish.offset(1, 1, 0), LuckyTheme.CASINO, defaultLuck + 5, random);
        placeLucky(level, finish.offset(1, 1, 1), LuckyTheme.SPEEDRUNNER, defaultLuck + 5, random);

        BlockPos startPad = laneOrigin.offset(-1, -1, -1);
        for (int dx = 0; dx < 3; dx++) {
            for (int dz = 0; dz < 3; dz++) {
                BlockPos p = startPad.offset(dx, 0, dz);
                level.setBlock(p.below(), Blocks.BEDROCK.defaultBlockState(), 3);
                level.setBlock(p, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            }
        }
        level.setBlock(laneOrigin.above(), Blocks.LANTERN.defaultBlockState(), 3);
        placeLucky(level, laneOrigin.offset(0, 2, 0), LuckyTheme.BOULANGERIE, defaultLuck, random);
    }

    private static BlockPos laneOriginAt(BlockPos arenaOrigin, int laneIndex, int laneCount) {
        double centerLane = (laneCount - 1) / 2.0;
        int z = arenaOrigin.getZ() + (int) Math.round((laneIndex - centerLane) * LANE_SPACING);
        return new BlockPos(arenaOrigin.getX(), arenaOrigin.getY() + FLOOR_Y_OFFSET, z);
    }

    private static boolean isGap(int step) {
        return step == 17 || step == 18 || step == 35 || step == 36;
    }

    private static LuckyTheme pickTheme(int lane, int step, RandomSource random) {
        LuckyTheme[] themes = LuckyTheme.values();
        int idx = Math.floorMod(lane * 17 + step * 3 + random.nextInt(themes.length), themes.length);
        return themes[idx];
    }

    private static void placeLucky(
            ServerLevel level,
            BlockPos pos,
            LuckyTheme theme,
            int luck,
            RandomSource random
    ) {
        BlockState lucky = ModBlocks.get(theme).defaultBlockState();
        level.setBlock(pos, lucky, 3);
        if (level.getBlockEntity(pos) instanceof LuckyBlockEntity be) {
            be.setLuck(luck + random.nextInt(5));
        }
    }
}
