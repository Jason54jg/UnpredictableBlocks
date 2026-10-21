package com.jason54.unpredictableblocks.command;

import com.jason54.unpredictableblocks.util.ModMessages;
import com.jason54.unpredictableblocks.util.ServerLevels;
import com.jason54.unpredictableblocks.world.NextWorldProfileStorage;
import com.jason54.unpredictableblocks.world.WorldModProfile;
import com.jason54.unpredictableblocks.world.WorldPlayMode;
import com.jason54.unpredictableblocks.world.WorldProfileApplier;
import com.jason54.unpredictableblocks.world.WorldSetupHandler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class WorldProfileCommands {
    private static final DynamicCommandExceptionType UNKNOWN_MODE =
            new DynamicCommandExceptionType(id -> ModMessages.msg("world.unknown_mode", id));

    private WorldProfileCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> branch() {
        return Commands.literal("world")
                .then(Commands.literal("next")
                        .then(Commands.argument("mode", StringArgumentType.word())
                                .executes(ctx -> saveNext(ctx.getSource(), parseMode(StringArgumentType.getString(ctx, "mode")), 1))
                                .then(Commands.argument("players", IntegerArgumentType.integer(1, 4))
                                        .executes(ctx -> saveNext(
                                                ctx.getSource(),
                                                parseMode(StringArgumentType.getString(ctx, "mode")),
                                                IntegerArgumentType.getInteger(ctx, "players")
                                        )))))
                .then(Commands.literal("setup")
                        .then(Commands.argument("mode", StringArgumentType.word())
                                .executes(ctx -> applySetup(ctx.getSource(), parseMode(StringArgumentType.getString(ctx, "mode")), 1))
                                .then(Commands.argument("players", IntegerArgumentType.integer(1, 4))
                                        .executes(ctx -> applySetup(
                                                ctx.getSource(),
                                                parseMode(StringArgumentType.getString(ctx, "mode")),
                                                IntegerArgumentType.getInteger(ctx, "players")
                                        )))))
                .then(Commands.literal("show")
                        .executes(ctx -> show(ctx.getSource())))
                .then(Commands.literal("menu")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            WorldSetupHandler.sendSetupMenu(player);
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    private static int saveNext(CommandSourceStack source, WorldPlayMode mode, int players) {
        NextWorldProfileStorage.save(new NextWorldProfileStorage.Pending(mode, players));
        source.sendSuccess(() -> ModMessages.msg("world.next_saved", mode.id(), players), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int applySetup(CommandSourceStack source, WorldPlayMode mode, int players) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = ServerLevels.of(player);
        WorldProfileApplier.applyAndAnnounce(level, mode, players, player);
        return Command.SINGLE_SUCCESS;
    }

    private static int show(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        WorldModProfile.WorldProfileData profile = WorldModProfile.get(ServerLevels.of(player));
        source.sendSuccess(() -> ModMessages.msg(
                "world.show",
                profile.configured() ? "yes" : "no",
                profile.mode().id(),
                profile.oneBlockPlayers(),
                profile.spawnMultiplier(),
                profile.defaultBlockLuck()
        ), false);
        NextWorldProfileStorage.peek().ifPresent(p ->
                source.sendSuccess(() -> ModMessages.msg("world.next_peek", p.mode().id(), p.oneBlockPlayers()), false)
        );
        return Command.SINGLE_SUCCESS;
    }

    private static WorldPlayMode parseMode(String raw) throws CommandSyntaxException {
        return WorldPlayMode.parse(raw).orElseThrow(() -> UNKNOWN_MODE.create(raw));
    }
}
