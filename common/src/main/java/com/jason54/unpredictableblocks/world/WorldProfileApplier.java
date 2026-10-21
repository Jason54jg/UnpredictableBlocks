package com.jason54.unpredictableblocks.world;

import com.jason54.unpredictableblocks.util.ModMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Applique le profil monde (OneBlock, messages, bordure). */
public final class WorldProfileApplier {
    private WorldProfileApplier() {}

    public static void apply(ServerLevel level, WorldModProfile.WorldProfileData profile) {
        apply(level, profile, defaultCenter(level));
    }

    public static void apply(ServerLevel level, WorldModProfile.WorldProfileData profile, BlockPos center) {
        BlockPos origin = arenaOrigin(profile, center);
        OneBlockArenaBuilder.buildIfNeeded(level, profile, origin);
        ParcoursArenaBuilder.buildIfNeeded(level, profile, origin);
        applyWorldBorder(level, profile, origin);
    }

    public static BlockPos arenaOrigin(WorldModProfile.WorldProfileData profile, BlockPos suggested) {
        if (profile.mode() == WorldPlayMode.PARCOURS) {
            return defaultCenter();
        }
        return suggested;
    }

    private static void applyWorldBorder(ServerLevel level, WorldModProfile.WorldProfileData profile, BlockPos center) {
        var border = level.getWorldBorder();
        if (profile.mode() == WorldPlayMode.ONEBLOCK) {
            double size = 16 + profile.oneBlockPlayers() * 12.0;
            border.setCenter(center.getX() + 0.5, center.getZ() + 0.5);
            border.setSize(size);
        } else if (profile.mode() == WorldPlayMode.PARCOURS) {
            int lanes = profile.oneBlockPlayers();
            double zSpan = Math.max(24, lanes * ParcoursArenaBuilder.LANE_SPACING + 16);
            double xSpan = ParcoursArenaBuilder.PATH_LENGTH + 24;
            border.setCenter(center.getX() + ParcoursArenaBuilder.PATH_LENGTH / 2.0, center.getZ() + 0.5);
            border.setSize(Math.max(xSpan, zSpan));
        }
    }

    private static BlockPos defaultCenter() {
        return new BlockPos(0, 80, 0);
    }

    private static BlockPos defaultCenter(ServerLevel level) {
        return defaultCenter();
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
        if (mode == WorldPlayMode.PARCOURS && profile.parcoursBuilt()) {
            WorldSetupHandler.sendToParcoursLane(player, profile);
        }
    }
}
