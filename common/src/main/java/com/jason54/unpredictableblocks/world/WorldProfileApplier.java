package com.jason54.unpredictableblocks.world;

import com.jason54.unpredictableblocks.util.ModMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;

/** Applique le profil monde (OneBlock, messages, bordure, spawn). */
public final class WorldProfileApplier {
    /** Hauteur hors collines / océan pour arènes construites. */
    public static final int ARENA_Y = 100;

    private WorldProfileApplier() {}

    public static void apply(ServerLevel level, WorldModProfile.WorldProfileData profile) {
        apply(level, profile, defaultCenter());
    }

    public static void apply(ServerLevel level, WorldModProfile.WorldProfileData profile, BlockPos center) {
        BlockPos origin = arenaOrigin(profile, center);
        // Force le chargement des chunks avant construction.
        level.getChunkAt(origin);
        OneBlockArenaBuilder.buildIfNeeded(level, profile, origin);
        ParcoursArenaBuilder.buildIfNeeded(level, profile, origin);
        applyWorldBorder(level, profile, origin);
        applyWorldSpawn(level, profile, origin);
        if (profile.mode() == WorldPlayMode.PARCOURS || profile.mode() == WorldPlayMode.ONEBLOCK) {
            ParcoursWorldState.get(level).setSessionOpen(true);
        }
    }

    public static BlockPos arenaOrigin(WorldModProfile.WorldProfileData profile, BlockPos suggested) {
        if (profile.mode() == WorldPlayMode.PARCOURS || profile.mode() == WorldPlayMode.ONEBLOCK) {
            return defaultCenter();
        }
        return suggested;
    }

    public static BlockPos playerSpawnPos(WorldModProfile.WorldProfileData profile, ServerPlayer player) {
        BlockPos origin = arenaOrigin(profile, defaultCenter());
        if (profile.mode() == WorldPlayMode.PARCOURS) {
            int lanes = Math.clamp(profile.oneBlockPlayers(), 1, 4);
            if (profile.modeSettings().parcoursLayout() == WorldModeSettings.ParcoursLayout.COOP) {
                lanes = 1;
            }
            int lane = ParcoursArenaBuilder.laneForPlayer(player.getUUID(), lanes);
            return ParcoursArenaBuilder.laneSpawnPos(origin, lane, lanes);
        }
        if (profile.mode() == WorldPlayMode.ONEBLOCK) {
            return OneBlockArenaBuilder.spawnPos(origin);
        }
        return origin;
    }

    public static void teleportToArena(ServerPlayer player, WorldModProfile.WorldProfileData profile) {
        if (profile.mode() != WorldPlayMode.PARCOURS && profile.mode() != WorldPlayMode.ONEBLOCK) {
            return;
        }
        BlockPos spawn = playerSpawnPos(profile, player);
        ServerLevel overworld = player.level().getServer().overworld();
        overworld.getChunkAt(spawn);
        player.teleportTo(
                overworld,
                spawn.getX() + 0.5,
                spawn.getY(),
                spawn.getZ() + 0.5,
                java.util.Set.of(),
                0.0F,
                0.0F,
                true
        );
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
    }

    private static void applyWorldSpawn(ServerLevel level, WorldModProfile.WorldProfileData profile, BlockPos origin) {
        if (profile.mode() != WorldPlayMode.PARCOURS && profile.mode() != WorldPlayMode.ONEBLOCK) {
            return;
        }
        BlockPos spawn = profile.mode() == WorldPlayMode.PARCOURS
                ? ParcoursArenaBuilder.laneSpawnPos(origin, 0, Math.max(1, profile.oneBlockPlayers()))
                : OneBlockArenaBuilder.spawnPos(origin);
        level.setRespawnData(LevelData.RespawnData.of(Level.OVERWORLD, spawn, 0.0F, 0.0F));
    }

    private static void applyWorldBorder(ServerLevel level, WorldModProfile.WorldProfileData profile, BlockPos center) {
        var border = level.getWorldBorder();
        if (profile.mode() == WorldPlayMode.ONEBLOCK) {
            double size = 16 + profile.oneBlockPlayers() * 12.0;
            border.setCenter(center.getX() + 0.5, center.getZ() + 0.5);
            border.setSize(size);
        } else if (profile.mode() == WorldPlayMode.PARCOURS) {
            int lanes = profile.oneBlockPlayers();
            int length = profile.modeSettings().pathLengthBlocks();
            double zSpan = Math.max(24, lanes * ParcoursArenaBuilder.LANE_SPACING + 16);
            double xSpan = length + 24;
            border.setCenter(center.getX() + length / 2.0, center.getZ() + 0.5);
            border.setSize(Math.max(xSpan, zSpan));
        }
    }

    private static BlockPos defaultCenter() {
        return new BlockPos(0, ARENA_Y, 0);
    }

    public static void applyAndAnnounce(
            ServerLevel level,
            WorldPlayMode mode,
            int oneBlockPlayers,
            ServerPlayer player
    ) {
        WorldModProfile.WorldProfileData profile = WorldModProfile.get(level);
        WorldModProfile.configure(profile, mode, oneBlockPlayers);
        apply(level, profile, arenaOrigin(profile, player.blockPosition()));
        profile.setConfigured(true);
        profile.setDirty();
        player.sendSystemMessage(ModMessages.msg(
                "world.profile_applied",
                mode.id(),
                profile.oneBlockPlayers()
        ));
        if (mode == WorldPlayMode.PARCOURS || mode == WorldPlayMode.ONEBLOCK) {
            ModeGameplayHandler.onSessionStart(player, profile);
        }
    }
}
