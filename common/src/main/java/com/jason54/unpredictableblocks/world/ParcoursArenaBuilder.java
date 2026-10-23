package com.jason54.unpredictableblocks.world;

import com.jason54.unpredictableblocks.block.entity.LuckyBlockEntity;
import com.jason54.unpredictableblocks.compat.McRefs;
import com.jason54.unpredictableblocks.registry.ModBlocks;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Parcours Lucky Block : pistes parallèles, coop, battle, roulette, checkpoints. */
public final class ParcoursArenaBuilder {
    public static final int LANE_SPACING = 12;
    public static final int FLOOR_Y_OFFSET = 0;
    /** NOTIFY_CLIENTS seul — évite les maj voisins qui forcent des chunks (freeze NeoForge). */
    private static final int PLACE_FLAGS = 2;

    private ParcoursArenaBuilder() {}

    public static void buildIfNeeded(ServerLevel level, WorldModProfile.WorldProfileData profile, BlockPos arenaOrigin) {
        if (profile.mode() != WorldPlayMode.PARCOURS || profile.parcoursBuilt()) {
            return;
        }
        WorldModeSettings settings = profile.modeSettings();
        long seed = settings.layoutSeed(level.getSeed());
        ParcoursWorldState.get(level).setLayoutSeed(seed);
        RandomSource random = RandomSource.create(seed);
        int lanes = laneCount(profile, settings);
        // Pas de clear envelope massif : ça force la gen de dizaines de chunks au boot
        // (freeze NeoForge « map qui charge pas »). clearAirColumn par case suffit.
        if (settings.parcoursLayout() == WorldModeSettings.ParcoursLayout.COOP) {
            buildCoopLane(level, arenaOrigin, profile, settings, random);
        } else {
            for (int lane = 0; lane < lanes; lane++) {
                buildLane(level, arenaOrigin, lane, lanes, profile, settings, random);
            }
        }
        profile.setParcoursBuilt(true);
        profile.setDirty();
    }

    public static void buildMiniBridge(ServerLevel level, BlockPos fromCenter, WorldModProfile.WorldProfileData profile) {
        WorldModeSettings settings = profile.modeSettings();
        BlockPos start = fromCenter.offset(3, 0, 0);
        for (int step = 0; step < 12; step++) {
            BlockPos foot = start.offset(step, 0, 0);
            level.setBlock(foot.below(), Blocks.STONE_BRICKS.defaultBlockState(), PLACE_FLAGS);
            if (step % 4 == 2) {
                placeLucky(level, foot.above(), LuckyTheme.FAST_FOOD, profile.defaultBlockLuck(), level.getRandom(), settings);
            }
        }
    }

    public static void syncCoopGates(ServerLevel level, WorldModProfile.WorldProfileData profile, BlockPos origin) {
        WorldModeSettings settings = profile.modeSettings();
        int length = settings.pathLengthBlocks();
        int[] gateSteps = {length / 3, (length * 2) / 3};
        var session = ParcoursWorldState.get(level);
        BlockPos laneOrigin = laneOriginAt(origin, 0, 1);
        for (int g = 0; g < gateSteps.length; g++) {
            if (session.gateOpen(g)) {
                clearGate(level, laneOrigin, gateSteps[g]);
            }
        }
    }

    /** Pieds du joueur sur le pad de départ (herbe en Y-1), hors lanterne / lucky. */
    public static BlockPos laneSpawnPos(BlockPos arenaOrigin, int laneIndex, int laneCount) {
        return laneOriginAt(arenaOrigin, laneIndex, laneCount).offset(-1, 0, 0);
    }

    public static int laneForPlayer(java.util.UUID playerId, int laneCount) {
        return Math.floorMod(playerId.getLeastSignificantBits(), laneCount);
    }

    public static int stepAt(BlockPos origin, int laneIndex, int laneCount, BlockPos feet) {
        BlockPos laneOrigin = laneOriginAt(origin, laneIndex, laneCount);
        return feet.getX() - laneOrigin.getX();
    }

    public static boolean isAtFinish(
            BlockPos origin,
            int laneIndex,
            int laneCount,
            WorldModProfile.WorldProfileData profile,
            BlockPos feet
    ) {
        int length = profile.modeSettings().pathLengthBlocks();
        int step = stepAt(origin, laneIndex, laneCount, feet);
        return step >= length && step <= length + 2;
    }

    public static BlockPos checkpointPos(
            BlockPos origin,
            int laneIndex,
            int laneCount,
            WorldModProfile.WorldProfileData profile,
            int step
    ) {
        int clamped = Math.max(0, Math.min(step, profile.modeSettings().pathLengthBlocks()));
        return laneOriginAt(origin, laneIndex, laneCount).offset(clamped, 0, 0);
    }

    private static int laneCount(WorldModProfile.WorldProfileData profile, WorldModeSettings settings) {
        if (settings.parcoursLayout() == WorldModeSettings.ParcoursLayout.COOP) {
            return 1;
        }
        return Math.clamp(profile.oneBlockPlayers(), 1, 4);
    }

    private static void buildCoopLane(
            ServerLevel level,
            BlockPos origin,
            WorldModProfile.WorldProfileData profile,
            WorldModeSettings settings,
            RandomSource random
    ) {
        BlockPos laneOrigin = laneOriginAt(origin, 0, 1);
        buildPath(
                level,
                laneOrigin,
                0,
                1,
                profile,
                settings,
                random,
                3,
                true
        );
    }

    private static void buildLane(
            ServerLevel level,
            BlockPos origin,
            int laneIndex,
            int laneCount,
            WorldModProfile.WorldProfileData profile,
            WorldModeSettings settings,
            RandomSource random
    ) {
        BlockPos laneOrigin = laneOriginAt(origin, laneIndex, laneCount);
        boolean battle = settings.parcoursLayout() == WorldModeSettings.ParcoursLayout.BATTLE;
        buildPath(level, laneOrigin, laneIndex, laneCount, profile, settings, random, battle ? 0 : 1, false);
    }

    private static void buildPath(
            ServerLevel level,
            BlockPos laneOrigin,
            int laneIndex,
            int laneCount,
            WorldModProfile.WorldProfileData profile,
            WorldModeSettings settings,
            RandomSource random,
            int pathHalfWidth,
            boolean coopGates
    ) {
        int pathLength = settings.pathLengthBlocks();
        int luckyInterval = settings.luckyStepInterval();
        BlockState path = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState accent = McRefs.LIGHT_GRAY_CONCRETE.defaultBlockState();
        BlockState rail = Blocks.OAK_FENCE.defaultBlockState();
        BlockState lowRail = Blocks.COBBLESTONE_WALL.defaultBlockState();
        int luck = profile.defaultBlockLuck();
        if (settings.parcoursRules() == WorldModeSettings.ParcoursRules.HARDCORE) {
            luck -= 5;
        }

        for (int step = -2; step <= pathLength; step++) {
            for (int dz = -pathHalfWidth; dz <= pathHalfWidth; dz++) {
                BlockPos foot = laneOrigin.offset(step, 0, dz);
                clearAirColumn(level, foot, 5);
                decorateUnderPath(level, foot, settings);
                if (isGap(step, pathLength)) {
                    if (step == gapBridgeStep(pathLength, 1)) {
                        level.setBlock(foot.below(), Blocks.STONE_BRICK_SLAB.defaultBlockState(), PLACE_FLAGS);
                    }
                    continue;
                }
                level.setBlock(foot.below(), path, PLACE_FLAGS);
                // Accents / sea lantern décalés pour ne pas boucher la case de spawn (step 0).
                if (dz == 0 && step > 0 && step % 6 == 0) {
                    level.setBlock(foot, accent, PLACE_FLAGS);
                }
                if (dz == 0 && step >= 0 && step % luckyInterval == 2) {
                    LuckyTheme theme = pickTheme(laneIndex, step, random, settings);
                    placeLucky(level, foot.above(), theme, luck, random, settings);
                    if (settings.parcoursRules() == WorldModeSettings.ParcoursRules.ROULETTE) {
                        level.setBlock(foot.offset(0, 2, 0), rouletteMarker(theme), PLACE_FLAGS);
                    }
                }
                if (step > 0 && step % 12 == 0 && dz == 0) {
                    level.setBlock(foot.above(), Blocks.SEA_LANTERN.defaultBlockState(), PLACE_FLAGS);
                }
                if (Math.abs(dz) == pathHalfWidth) {
                    level.setBlock(foot.offset(0, 1, 0), pathHalfWidth > 1 ? lowRail : rail, PLACE_FLAGS);
                }
            }
            if (coopGates && (step == pathLength / 3 || step == (pathLength * 2) / 3)) {
                placeGate(level, laneOrigin, step, pathHalfWidth);
            }
        }

        BlockPos finish = laneOrigin.offset(pathLength + 1, 0, 0);
        for (int dx = 0; dx <= 2; dx++) {
            for (int dz = -pathHalfWidth; dz <= pathHalfWidth; dz++) {
                BlockPos p = finish.offset(dx, -1, dz);
                decorateUnderPath(level, p, settings);
                level.setBlock(p, dx == 1 && dz == 0 ? Blocks.GOLD_BLOCK.defaultBlockState() : path, PLACE_FLAGS);
            }
        }
        placeLucky(level, finish.offset(1, 1, 0), LuckyTheme.JDR_D20, luck + 5, random, settings);
        placeLucky(level, finish.offset(1, 1, 1), LuckyTheme.SPEEDRUNNER, luck + 5, random, settings);

        BlockPos startPad = laneOrigin.offset(-1, -1, -pathHalfWidth);
        int pad = 2 + pathHalfWidth * 2;
        for (int dx = 0; dx < pad; dx++) {
            for (int dz = 0; dz < pad; dz++) {
                BlockPos p = startPad.offset(dx, 0, dz);
                clearAirColumn(level, p.above(), 4);
                decorateUnderPath(level, p.below(), settings);
                level.setBlock(p, Blocks.GRASS_BLOCK.defaultBlockState(), PLACE_FLAGS);
            }
        }
        // Lanterne / lucky de départ sur le côté — pas sur la case de spawn.
        level.setBlock(laneOrigin.offset(-1, 1, pathHalfWidth + 1), Blocks.LANTERN.defaultBlockState(), PLACE_FLAGS);
        placeLucky(level, laneOrigin.offset(1, 1, 0), LuckyTheme.BOULANGERIE, luck, random, settings);
    }

    private static void clearAirColumn(ServerLevel level, BlockPos base, int height) {
        for (int y = 0; y < height; y++) {
            BlockPos p = base.above(y);
            BlockState state = level.getBlockState(p);
            if (!state.isAir() && !state.is(Blocks.BEDROCK)) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), PLACE_FLAGS);
            }
        }
    }

    private static void decorateUnderPath(ServerLevel level, BlockPos foot, WorldModeSettings settings) {
        level.setBlock(foot.below(2), Blocks.BEDROCK.defaultBlockState(), PLACE_FLAGS);
        if (settings.arenaDecor() == WorldModeSettings.ArenaDecor.ISLAND) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos p = foot.offset(dx, -3, dz);
                    level.setBlock(p, Blocks.DIRT.defaultBlockState(), PLACE_FLAGS);
                    level.setBlock(p.below(), Blocks.STONE.defaultBlockState(), PLACE_FLAGS);
                }
            }
        }
    }

    private static void placeGate(ServerLevel level, BlockPos laneOrigin, int step, int pathHalfWidth) {
        for (int dz = -pathHalfWidth; dz <= pathHalfWidth; dz++) {
            BlockPos bar = laneOrigin.offset(step, 1, dz);
            level.setBlock(bar, Blocks.IRON_BARS.defaultBlockState(), PLACE_FLAGS);
        }
    }

    private static void clearGate(ServerLevel level, BlockPos laneOrigin, int step) {
        for (int dz = -3; dz <= 3; dz++) {
            BlockPos bar = laneOrigin.offset(step, 1, dz);
            if (level.getBlockState(bar).is(Blocks.IRON_BARS)) {
                level.setBlock(bar, Blocks.AIR.defaultBlockState(), PLACE_FLAGS);
            }
        }
    }

    private static BlockPos laneOriginAt(BlockPos arenaOrigin, int laneIndex, int laneCount) {
        double centerLane = (laneCount - 1) / 2.0;
        int z = arenaOrigin.getZ() + (int) Math.round((laneIndex - centerLane) * LANE_SPACING);
        return new BlockPos(arenaOrigin.getX(), arenaOrigin.getY() + FLOOR_Y_OFFSET, z);
    }

    private static boolean isGap(int step, int pathLength) {
        int g1 = pathLength / 3;
        int g2 = (pathLength * 2) / 3;
        return step == g1 || step == g1 + 1 || step == g2 || step == g2 + 1;
    }

    private static int gapBridgeStep(int pathLength, int which) {
        return which == 1 ? pathLength / 3 + 1 : (pathLength * 2) / 3 + 1;
    }

    private static LuckyTheme pickTheme(int lane, int step, RandomSource random, WorldModeSettings settings) {
        LuckyTheme[] themes = LuckyTheme.values();
        if (settings.parcoursRules() == WorldModeSettings.ParcoursRules.ROULETTE) {
            int idx = Math.floorMod(lane * 31 + step * 7, themes.length);
            return themes[idx];
        }
        int idx = Math.floorMod(lane * 17 + step * 3 + random.nextInt(themes.length), themes.length);
        return themes[idx];
    }

    private static BlockState rouletteMarker(LuckyTheme theme) {
        int idx = theme.ordinal() % 16;
        return switch (idx % 4) {
            case 0 -> McRefs.RED_WOOL.defaultBlockState();
            case 1 -> McRefs.BLUE_WOOL.defaultBlockState();
            case 2 -> McRefs.YELLOW_WOOL.defaultBlockState();
            default -> McRefs.LIME_WOOL.defaultBlockState();
        };
    }

    private static void placeLucky(
            ServerLevel level,
            BlockPos pos,
            LuckyTheme theme,
            int luck,
            RandomSource random,
            WorldModeSettings settings
    ) {
        BlockState lucky = ModBlocks.get(theme).defaultBlockState();
        level.setBlock(pos, lucky, PLACE_FLAGS);
        if (level.getBlockEntity(pos) instanceof LuckyBlockEntity be) {
            int offset = settings.luckyDensity() == WorldModeSettings.LuckyDensity.CARNIVAL ? random.nextInt(8) : random.nextInt(5);
            be.setLuck(luck + offset);
        }
    }
}
