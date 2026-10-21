package com.jason54.unpredictableblocks.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class ServerLevels {
    private ServerLevels() {}

    public static ServerLevel of(ServerPlayer player) {
        return (ServerLevel) player.level();
    }

    public static MinecraftServer server(ServerPlayer player) {
        return of(player).getServer();
    }
}
