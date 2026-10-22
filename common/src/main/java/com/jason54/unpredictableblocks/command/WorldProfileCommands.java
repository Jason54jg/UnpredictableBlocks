package com.jason54.unpredictableblocks.command;

import com.jason54.unpredictableblocks.util.ModMessages;
import com.jason54.unpredictableblocks.util.ServerLevels;
import com.jason54.unpredictableblocks.world.ArenaRebuild;
import com.jason54.unpredictableblocks.world.ModeGameplayHandler;
import com.jason54.unpredictableblocks.world.NextWorldProfileStorage;
import com.jason54.unpredictableblocks.world.ParcoursWorldState;
import com.jason54.unpredictableblocks.world.WorldModProfile;
import com.jason54.unpredictableblocks.world.WorldModePresets;
import com.jason54.unpredictableblocks.world.WorldModeSettings;
import com.jason54.unpredictableblocks.world.WorldPlayMode;
import com.jason54.unpredictableblocks.world.WorldProfileApplier;
import com.jason54.unpredictableblocks.world.WorldSetupHandler;
import com.jason54.unpredictableblocks.world.WorldVoteManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class WorldProfileCommands {
    private static final DynamicCommandExceptionType UNKNOWN_MODE =
            new DynamicCommandExceptionType(id -> ModMessages.msg("world.unknown_mode", id));
    private static final DynamicCommandExceptionType UNKNOWN_PRESET =
            new DynamicCommandExceptionType(id -> ModMessages.msg("world.unknown_preset", id));

    private WorldProfileCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> branch() {
        return Commands.literal("world")
                .then(Commands.literal("next")
                        .then(Commands.argument("mode", StringArgumentType.word())
                                .executes(ctx -> saveNext(ctx.getSource(), parseMode(StringArgumentType.getString(ctx, "mode")), 1, null))
                                .then(Commands.argument("players", IntegerArgumentType.integer(1, 4))
                                        .executes(ctx -> saveNext(
                                                ctx.getSource(),
                                                parseMode(StringArgumentType.getString(ctx, "mode")),
                                                IntegerArgumentType.getInteger(ctx, "players"),
                                                null
                                        )))))
                .then(Commands.literal("preset")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(ctx -> savePreset(ctx.getSource(), StringArgumentType.getString(ctx, "name"), 1))
                                .then(Commands.argument("players", IntegerArgumentType.integer(1, 4))
                                        .executes(ctx -> savePreset(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "name"),
                                                IntegerArgumentType.getInteger(ctx, "players")
                                        )))))
                .then(Commands.literal("setup")
                        .then(Commands.argument("mode", StringArgumentType.word())
                                .executes(ctx -> applySetup(ctx.getSource(), parseMode(StringArgumentType.getString(ctx, "mode")), 1, null))
                                .then(Commands.argument("players", IntegerArgumentType.integer(1, 4))
                                        .executes(ctx -> applySetup(
                                                ctx.getSource(),
                                                parseMode(StringArgumentType.getString(ctx, "mode")),
                                                IntegerArgumentType.getInteger(ctx, "players"),
                                                null
                                        )))))
                .then(Commands.literal("options")
                        .then(Commands.literal("length")
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .executes(ctx -> setOption(ctx.getSource(), o -> o.withLength(StringArgumentType.getString(ctx, "value"))))))
                        .then(Commands.literal("density")
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .executes(ctx -> setOption(ctx.getSource(), o -> o.withDensity(StringArgumentType.getString(ctx, "value"))))))
                        .then(Commands.literal("rules")
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .executes(ctx -> setOption(ctx.getSource(), o -> o.withRules(StringArgumentType.getString(ctx, "value"))))))
                        .then(Commands.literal("layout")
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .executes(ctx -> setOption(ctx.getSource(), o -> o.withLayout(StringArgumentType.getString(ctx, "value"))))))
                        .then(Commands.literal("decor")
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .executes(ctx -> setOption(ctx.getSource(), o -> o.withDecor(StringArgumentType.getString(ctx, "value"))))))
                        .then(Commands.literal("daily")
                                .executes(ctx -> setOption(ctx.getSource(), WorldModeOptionsMutator::toggleDaily))))
                .then(Commands.literal("vote")
                        .then(Commands.literal("start")
                                .then(Commands.argument("preset", StringArgumentType.word())
                                        .executes(ctx -> voteStart(ctx.getSource(), StringArgumentType.getString(ctx, "preset"), 1))
                                        .then(Commands.argument("players", IntegerArgumentType.integer(1, 4))
                                                .executes(ctx -> voteStart(
                                                        ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "preset"),
                                                        IntegerArgumentType.getInteger(ctx, "players")
                                                )))))
                        .then(Commands.literal("yes")
                                .executes(ctx -> voteCast(ctx.getSource(), "yes")))
                        .then(Commands.literal("no")
                                .executes(ctx -> voteCast(ctx.getSource(), "no")))
                        .then(Commands.literal("confirm")
                                .executes(ctx -> voteConfirm(ctx.getSource()))))
                .then(Commands.literal("start")
                        .executes(ctx -> sessionStart(ctx.getSource())))
                .then(Commands.literal("rebuild")
                        .executes(ctx -> rebuildArena(ctx.getSource())))
                .then(Commands.literal("show")
                        .executes(ctx -> show(ctx.getSource())))
                .then(Commands.literal("menu")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            WorldSetupHandler.sendSetupMenu(player);
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    private static int saveNext(CommandSourceStack source, WorldPlayMode mode, int players, WorldModeSettings settings) {
        NextWorldProfileStorage.save(new NextWorldProfileStorage.Pending(mode, players, settings));
        source.sendSuccess(() -> ModMessages.msg("world.next_saved", mode.id(), players), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int savePreset(CommandSourceStack source, String preset, int players) throws CommandSyntaxException {
        NextWorldProfileStorage.Pending pending = WorldModePresets.pending(preset, players)
                .orElseThrow(() -> UNKNOWN_PRESET.create(preset));
        NextWorldProfileStorage.save(pending);
        source.sendSuccess(() -> ModMessages.msg("world.preset_saved", preset, players), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int applySetup(CommandSourceStack source, WorldPlayMode mode, int players, WorldModeSettings settings) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = ServerLevels.of(player);
        WorldModProfile.WorldProfileData profile = WorldModProfile.get(level);
        WorldModProfile.configure(profile, mode, players, settings);
        WorldProfileApplier.apply(level, profile, WorldProfileApplier.arenaOrigin(profile, player.blockPosition()));
        profile.setConfigured(true);
        profile.setDirty();
        player.sendSystemMessage(ModMessages.msg("world.profile_applied", mode.id(), players));
        if (mode == WorldPlayMode.PARCOURS && profile.parcoursBuilt()) {
            ModeGameplayHandler.onSessionStart(player, profile);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int setOption(CommandSourceStack source, java.util.function.Function<WorldModeOptionsMutator, WorldModeSettings> fn) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        WorldModProfile.WorldProfileData profile = WorldModProfile.get(ServerLevels.of(player));
        WorldModeSettings updated = fn.apply(new WorldModeOptionsMutator(profile.modeSettings()));
        profile.setModeSettings(updated);
        profile.setDirty();
        NextWorldProfileStorage.save(new NextWorldProfileStorage.Pending(profile.mode(), profile.oneBlockPlayers(), updated));
        source.sendSuccess(() -> ModMessages.msg("world.options_updated"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int voteStart(CommandSourceStack source, String preset, int players) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        NextWorldProfileStorage.Pending pending = WorldModePresets.pending(preset, players)
                .orElseThrow(() -> UNKNOWN_PRESET.create(preset));
        WorldVoteManager.startBallot(ServerLevels.of(player), pending);
        return Command.SINGLE_SUCCESS;
    }

    private static int voteCast(CommandSourceStack source, String option) throws CommandSyntaxException {
        WorldVoteManager.cast(source.getPlayerOrException(), option);
        return Command.SINGLE_SUCCESS;
    }

    private static int voteConfirm(CommandSourceStack source) throws CommandSyntaxException {
        WorldVoteManager.confirm(source.getPlayerOrException());
        return Command.SINGLE_SUCCESS;
    }

    private static int rebuildArena(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = ServerLevels.of(player);
        WorldModProfile.WorldProfileData profile = WorldModProfile.get(level);
        if (profile.mode() != WorldPlayMode.PARCOURS && profile.mode() != WorldPlayMode.ONEBLOCK) {
            source.sendFailure(ModMessages.msg("world.rebuild_wrong_mode"));
            return 0;
        }
        ArenaRebuild.rebuild(level, profile);
        if (profile.mode() == WorldPlayMode.PARCOURS) {
            level.getServer().getPlayerList().getPlayers().forEach(p -> ModeGameplayHandler.onSessionStart(p, profile));
        }
        source.sendSuccess(() -> ModMessages.msg("world.rebuild_done"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int sessionStart(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = ServerLevels.of(player);
        ParcoursWorldState.get(level).setSessionOpen(true);
        WorldModProfile.WorldProfileData profile = WorldModProfile.get(level);
        level.getServer().getPlayerList().getPlayers().forEach(p -> ModeGameplayHandler.onSessionStart(p, profile));
        source.sendSuccess(() -> ModMessages.msg("world.session_started"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int show(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        WorldModProfile.WorldProfileData profile = WorldModProfile.get(ServerLevels.of(player));
        WorldModeSettings s = profile.modeSettings();
        source.sendSuccess(() -> ModMessages.msg(
                "world.show",
                profile.configured() ? "yes" : "no",
                profile.mode().id(),
                profile.oneBlockPlayers(),
                profile.spawnMultiplier(),
                profile.defaultBlockLuck()
        ), false);
        source.sendSuccess(() -> ModMessages.msg(
                "world.show_options",
                s.pathScale().name(),
                s.luckyDensity().name(),
                s.parcoursLayout().name(),
                s.parcoursRules().name(),
                s.arenaDecor().name(),
                s.dailySeed() ? "yes" : "no"
        ), false);
        NextWorldProfileStorage.peek().ifPresent(p ->
                source.sendSuccess(() -> ModMessages.msg("world.next_peek", p.mode().id(), p.oneBlockPlayers()), false)
        );
        return Command.SINGLE_SUCCESS;
    }

    private static WorldPlayMode parseMode(String raw) throws CommandSyntaxException {
        return WorldPlayMode.parse(raw).orElseThrow(() -> UNKNOWN_MODE.create(raw));
    }

    /** Applique une mutation aux options monde (commande {@code world options}). */
    private record WorldModeOptionsMutator(WorldModeSettings base) {
        WorldModeSettings withLength(String raw) {
            WorldModeSettings.PathScale scale = WorldModeSettings.parsePathScale(raw)
                    .orElse(base.pathScale());
            return new WorldModeSettings(scale, base.luckyDensity(), base.parcoursLayout(), base.parcoursRules(),
                    base.arenaDecor(), base.dailySeed(), base.oneBlockEvolution());
        }

        WorldModeSettings withDensity(String raw) {
            WorldModeSettings.LuckyDensity d = WorldModeSettings.parseDensity(raw).orElse(base.luckyDensity());
            return new WorldModeSettings(base.pathScale(), d, base.parcoursLayout(), base.parcoursRules(),
                    base.arenaDecor(), base.dailySeed(), base.oneBlockEvolution());
        }

        WorldModeSettings withRules(String raw) {
            WorldModeSettings.ParcoursRules r = WorldModeSettings.parseRules(raw).orElse(base.parcoursRules());
            return new WorldModeSettings(base.pathScale(), base.luckyDensity(), base.parcoursLayout(), r,
                    base.arenaDecor(), base.dailySeed(), base.oneBlockEvolution());
        }

        WorldModeSettings withLayout(String raw) {
            WorldModeSettings.ParcoursLayout l = WorldModeSettings.parseLayout(raw).orElse(base.parcoursLayout());
            return new WorldModeSettings(base.pathScale(), base.luckyDensity(), l, base.parcoursRules(),
                    base.arenaDecor(), base.dailySeed(), base.oneBlockEvolution());
        }

        WorldModeSettings withDecor(String raw) {
            WorldModeSettings.ArenaDecor d = WorldModeSettings.parseDecor(raw).orElse(base.arenaDecor());
            return new WorldModeSettings(base.pathScale(), base.luckyDensity(), base.parcoursLayout(), base.parcoursRules(),
                    d, base.dailySeed(), base.oneBlockEvolution());
        }

        WorldModeSettings toggleDaily() {
            return new WorldModeSettings(base.pathScale(), base.luckyDensity(), base.parcoursLayout(), base.parcoursRules(),
                    base.arenaDecor(), !base.dailySeed(), base.oneBlockEvolution());
        }
    }
}
