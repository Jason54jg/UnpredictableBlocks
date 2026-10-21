package com.jason54.unpredictableblocks.world;

import com.jason54.unpredictableblocks.util.ModMessages;
import com.jason54.unpredictableblocks.util.ServerLevels;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/** Bootstrap profil à la création / 1er join (assistant si non configuré). */
public final class WorldSetupHandler {
    private static boolean promptedThisSession;

    private WorldSetupHandler() {}

    public static void register() {
        LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> {
            if (level instanceof ServerLevel serverLevel && serverLevel.dimension().equals(Level.OVERWORLD)) {
                WorldModProfile.bootstrapOverworld(serverLevel);
            }
        });

        PlayerEvent.PLAYER_JOIN.register(player -> {
            if (player.level().isClientSide() || !(player instanceof ServerPlayer sp)) {
                return;
            }
            ServerLevel overworld = ServerLevels.server(sp).overworld();
            WorldModProfile.WorldProfileData profile = WorldModProfile.get(overworld);
            if (profile.configured()) {
                return;
            }
            if (!canConfigure(sp)) {
                return;
            }
            if (promptedThisSession) {
                return;
            }
            promptedThisSession = true;
            sendSetupMenu(sp);
        });
    }

    private static boolean canConfigure(ServerPlayer player) {
        return ServerLevels.server(player).isSingleplayer();
    }

    public static void sendSetupMenu(ServerPlayer player) {
        player.sendSystemMessage(ModMessages.msg("world.setup_header"));
        player.sendSystemMessage(clickable("world.setup_normal", "/unpredictableblocks world setup normal"));
        player.sendSystemMessage(clickable("world.setup_easy", "/unpredictableblocks world setup easy"));
        player.sendSystemMessage(clickable("world.setup_hard", "/unpredictableblocks world setup hard"));
        player.sendSystemMessage(clickable("world.setup_hardcore", "/unpredictableblocks world setup hardcore"));
        player.sendSystemMessage(clickable("world.setup_oneblock_solo", "/unpredictableblocks world setup oneblock 1"));
        player.sendSystemMessage(clickable("world.setup_oneblock_duo", "/unpredictableblocks world setup oneblock 2"));
        player.sendSystemMessage(clickable("world.setup_oneblock_trio", "/unpredictableblocks world setup oneblock 3"));
        player.sendSystemMessage(clickable("world.setup_oneblock_squad", "/unpredictableblocks world setup oneblock 4"));
        NextWorldProfileStorage.peek().ifPresent(p ->
                player.sendSystemMessage(ModMessages.msg("world.setup_pending_hint", p.mode().id(), p.oneBlockPlayers()))
        );
    }

    private static MutableComponent clickable(String messageKey, String command) {
        return ModMessages.msg(messageKey).copy().withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand(command)));
    }
}
