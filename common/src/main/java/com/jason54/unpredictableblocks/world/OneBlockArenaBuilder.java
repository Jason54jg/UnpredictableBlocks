package com.jason54.unpredictableblocks.world;

import com.jason54.unpredictableblocks.block.LuckyBlock;
import com.jason54.unpredictableblocks.block.entity.LuckyBlockEntity;
import com.jason54.unpredictableblocks.registry.ModBlocks;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Plateforme OneBlock + Lucky Block générateur (respawn automatique). */
public final class OneBlockArenaBuilder {
    /** Délai entre deux respawns (ticks). */
    private static final int REGEN_TICKS = 40;
    /** NOTIFY_CLIENTS seul — évite les maj voisins (freeze NeoForge). */
    private static final int PLACE_FLAGS = 2;

    private OneBlockArenaBuilder() {}

    public static void register() {
        TickEvent.SERVER_POST.register(OneBlockArenaBuilder::tick);
    }

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
                level.setBlock(top.below(), Blocks.BEDROCK.defaultBlockState(), PLACE_FLAGS);
                level.setBlock(top, Blocks.GRASS_BLOCK.defaultBlockState(), PLACE_FLAGS);
            }
        }
        BlockPos gen = generatorPos(center);
        // Marqueur : or sous le générateur (comme Lucky Skyblock).
        level.setBlock(gen.below(), Blocks.GOLD_BLOCK.defaultBlockState(), PLACE_FLAGS);
        placeGeneratorLucky(level, gen, profile.defaultBlockLuck());
        profile.setOneBlockBuilt(true);
        profile.setDirty();
    }

    /** Case du Lucky générateur (à côté du spawn). */
    public static BlockPos generatorPos(BlockPos center) {
        return center.offset(1, 0, 0);
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
                level.setBlock(top.below(), Blocks.BEDROCK.defaultBlockState(), PLACE_FLAGS);
                level.setBlock(top, Blocks.GRASS_BLOCK.defaultBlockState(), PLACE_FLAGS);
            }
        }
    }

    /** Appelé juste après un break OneBlock pour respawn rapide. */
    public static void scheduleRespawn(ServerLevel level, WorldModProfile.WorldProfileData profile) {
        if (profile.mode() != WorldPlayMode.ONEBLOCK || !profile.oneBlockBuilt()) {
            return;
        }
        BlockPos origin = WorldProfileApplier.arenaOrigin(profile, new BlockPos(0, WorldProfileApplier.ARENA_Y, 0));
        BlockPos gen = generatorPos(origin);
        level.getServer().execute(() -> tryRespawn(level, gen, profile.defaultBlockLuck()));
    }

    private static void tick(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        if (overworld.getGameTime() % REGEN_TICKS != 0) {
            return;
        }
        WorldModProfile.WorldProfileData profile = WorldModProfile.get(overworld);
        if (profile.mode() != WorldPlayMode.ONEBLOCK || !profile.oneBlockBuilt()) {
            return;
        }
        BlockPos origin = WorldProfileApplier.arenaOrigin(profile, new BlockPos(0, WorldProfileApplier.ARENA_Y, 0));
        tryRespawn(overworld, generatorPos(origin), profile.defaultBlockLuck());
    }

    private static void tryRespawn(ServerLevel level, BlockPos gen, int luck) {
        if (!level.dimension().equals(Level.OVERWORLD) || !level.isLoaded(gen)) {
            return;
        }
        if (!level.getBlockState(gen).isAir()) {
            return;
        }
        if (!(level.getBlockState(gen.below()).is(Blocks.GOLD_BLOCK)
                || level.getBlockState(gen.below()).is(Blocks.GRASS_BLOCK)
                || level.getBlockState(gen.below()).is(Blocks.BEDROCK))) {
            // Restaure le socle si besoin.
            level.setBlock(gen.below(), Blocks.GOLD_BLOCK.defaultBlockState(), PLACE_FLAGS);
        }
        if (placeGeneratorLucky(level, gen, luck)) {
            level.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    gen.getX() + 0.5, gen.getY() + 0.5, gen.getZ() + 0.5,
                    10, 0.35, 0.35, 0.35, 0.0
            );
            level.playSound(null, gen, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8F, 1.15F);
        }
    }

    private static boolean placeGeneratorLucky(ServerLevel level, BlockPos gen, int luck) {
        if (!LuckyBlockSpawnPlacement.placeRandom(level, gen, level.getRandom())) {
            // Fallback thème fixe si le tirage config échoue (spawnMultiplier=0).
            BlockState lucky = ModBlocks.get(LuckyTheme.BOULANGERIE).defaultBlockState();
            if (!level.setBlock(gen, lucky, PLACE_FLAGS)) {
                return false;
            }
            if (level.getBlockEntity(gen) instanceof LuckyBlockEntity be) {
                be.setLuck(luck);
            }
            return true;
        }
        if (level.getBlockEntity(gen) instanceof LuckyBlockEntity be) {
            be.setLuck(luck);
        }
        return true;
    }

    private static void clearPlatformVolume(ServerLevel level, BlockPos center, int radius, int height) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -3; dy <= height; dy++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(p);
                    if (!state.isAir() && !state.is(Blocks.BEDROCK) && !(state.getBlock() instanceof LuckyBlock)) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), PLACE_FLAGS);
                    }
                }
            }
        }
    }
}
