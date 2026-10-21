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
        OneBlockArenaBuilder.buildIfNeeded(level, profile, center);
        if (profile.mode() == WorldPlayMode.ONEBLOCK) {
            var border = level.getWorldBorder();
            double size = 16 + profile.oneBlockPlayers() * 12.0;
            border.setCenter(center.getX() + 0.5, center.getZ() + 0.5);
            border.setSize(size);
        }
    }

    private static BlockPos defaultCenter(ServerLevel level) {
        return new BlockPos(0, 80, 0);
    }

    public static void applyAndAnnounce(
            ServerLevel level,
            WorldPlayMode mode,
            int oneBlockPlayers,
            ServerPlayer player
    ) {
        WorldModProfile.WorldProfileData profile = WorldModProfile.get(level);
        WorldModProfile.configure(profile, mode, oneBlockPlayers);
        apply(level, profile, player.blockPosition());
        profile.setConfigured(true);
        profile.setDirty();
        player.sendSystemMessage(ModMessages.msg(
                "world.profile_applied",
                mode.id(),
                profile.oneBlockPlayers()
        ));
    }
}
