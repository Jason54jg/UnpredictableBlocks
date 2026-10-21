package com.jason54.unpredictableblocks.command;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.config.ModConfig;
import com.jason54.unpredictableblocks.block.LuckyBlockEventDispatcher;
import com.jason54.unpredictableblocks.block.entity.LuckyBlockEntity;
import com.jason54.unpredictableblocks.luck.LuckResolver;
import com.jason54.unpredictableblocks.luck.LuckTier;
import com.jason54.unpredictableblocks.quest.CasinoJackpot;
import com.jason54.unpredictableblocks.quest.PlayerQuestData;
import com.jason54.unpredictableblocks.registry.ModBlocks;
import com.jason54.unpredictableblocks.registry.ModItems;
import com.jason54.unpredictableblocks.lucky.PlayerLuckyBreakStats;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import com.jason54.unpredictableblocks.util.ModMessages;
import com.jason54.unpredictableblocks.util.ServerLevels;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class UnpredictableBlocksCommands {
    private static final DynamicCommandExceptionType UNKNOWN_THEME =
            new DynamicCommandExceptionType(id -> ModMessages.msg("command.unknown_theme", id));

    private UnpredictableBlocksCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal(UnpredictableBlocks.MOD_ID)
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.literal("event")
                                .then(Commands.argument("theme", StringArgumentType.word())
                                        .then(Commands.argument("tier", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    LuckyTheme theme = resolveTheme(StringArgumentType.getString(ctx, "theme"));
                                                    LuckTier tier = LuckTier.valueOf(StringArgumentType.getString(ctx, "tier").toUpperCase());
                                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                                    BlockPos pos = player.blockPosition();
                                                    ServerLevel level = ServerLevels.of(player);
                                                    var outcome = new LuckResolver.ResolvedOutcome(tier, suffix(tier));
                                                    LuckyBlockEventDispatcher.dispatch(level, player, pos, theme, outcome);
                                                    ctx.getSource().sendSuccess(
                                                            () -> ModMessages.msg("command.event_ok", theme.id(), tier),
                                                            true
                                                    );
                                                    return 1;
                                                }))))
                        .then(Commands.literal("casino")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    CasinoJackpot.spin(ServerLevels.of(player), player, player.getRandom());
                                    return 1;
                                }))
                        .then(Commands.literal("place")
                                .then(Commands.argument("theme", StringArgumentType.word())
                                        .executes(ctx -> place(ctx.getSource(), resolveTheme(StringArgumentType.getString(ctx, "theme")), 0))
                                        .then(Commands.argument("luck", IntegerArgumentType.integer(-100, 100))
                                                .executes(ctx -> place(
                                                        ctx.getSource(),
                                                        resolveTheme(StringArgumentType.getString(ctx, "theme")),
                                                        IntegerArgumentType.getInteger(ctx, "luck")
                                                )))))
                        .then(Commands.literal("givekit")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    for (LuckyTheme theme : LuckyTheme.values()) {
                                        player.getInventory().add(new net.minecraft.world.item.ItemStack(ModBlocks.get(theme)));
                                    }
                                    player.getInventory().add(new net.minecraft.world.item.ItemStack(ModItems.JOURNAL_QUETES.get()));
                                    player.getInventory().add(new net.minecraft.world.item.ItemStack(ModItems.LUCKY_MYSTERE.get(), 3));
                                    player.getInventory().add(new net.minecraft.world.item.ItemStack(ModItems.CARTE_COLLECTION.get(), 8));
                                    return 1;
                                }))
                        .then(Commands.literal("reload")
                                .executes(ctx -> {
                                    ModConfig.load();
                                    ctx.getSource().sendSuccess(
                                            () -> ModMessages.msg("command.reload_ok"),
                                            true
                                    );
                                    return 1;
                                }))
                        .then(Commands.literal("gameplay")
                                .executes(ctx -> {
                                    var g = ModConfig.gameplay();
                                    ctx.getSource().sendSuccess(() -> ModMessages.msg("command.gameplay_header"), false);
                                    ctx.getSource().sendSuccess(() -> ModMessages.msg(
                                            "command.gameplay_line1",
                                            (int) (g.cardDropOnHighOrDefault() * 100),
                                            g.comboHitsRequiredOrDefault(),
                                            g.comboWindowTicksOrDefault() / 20
                                    ), false);
                                    ctx.getSource().sendSuccess(() -> ModMessages.msg(
                                            "command.gameplay_line2",
                                            g.nearMissLuckMinOrDefault(),
                                            g.nearMissLuckMaxOrDefault(),
                                            g.themeMasterBreaksOrDefault()
                                    ), false);
                                    ctx.getSource().sendSuccess(() -> ModMessages.msg(
                                            "command.gameplay_line3",
                                            g.malusStreakLengthOrDefault(),
                                            g.malusStreakLuckBonusOrDefault(),
                                            g.missedRerollTicksOrDefault() / 20
                                    ), false);
                                    ctx.getSource().sendSuccess(() -> ModMessages.msg(
                                            "command.gameplay_line4",
                                            g.luckBetAmountOrDefault(),
                                            g.ambientParticleRadiusOrDefault(),
                                            g.ambientParticleIntervalTicksOrDefault(),
                                            ModMessages.msg(g.useModTierSoundsOrDefault() ? "yes" : "no")
                                    ), false);
                                    return 1;
                                }))
                        .then(Commands.literal("stats")
                                .executes(ctx -> printStats(ctx.getSource(), null))
                                .then(Commands.argument("theme", StringArgumentType.word())
                                        .executes(ctx -> printStats(
                                                ctx.getSource(),
                                                resolveTheme(StringArgumentType.getString(ctx, "theme"))
                                        ))))
                        .then(WorldProfileCommands.branch())
                        .then(Commands.literal("transfer")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer from = ctx.getSource().getPlayerOrException();
                                            ServerPlayer to = EntityArgument.getPlayer(ctx, "target");
                                            if (from.getUUID().equals(to.getUUID())) {
                                                ctx.getSource().sendFailure(ModMessages.msg("command.invalid_target"));
                                                return 0;
                                            }
                                            if (!PlayerQuestData.hasMissedReroll(from)) {
                                                ctx.getSource().sendFailure(ModMessages.msg("command.transfer_none"));
                                                return 0;
                                            }
                                            if (PlayerQuestData.transferMissedReroll(from, to)) {
                                                ctx.getSource().sendSuccess(
                                                        () -> ModMessages.msg("command.transfer_ok", to.getGameProfile().name()),
                                                        true
                                                );
                                                to.sendSystemMessage(ModMessages.msg("vip_transfer_to", from.getGameProfile().name()));
                                                return 1;
                                            }
                                            ctx.getSource().sendFailure(ModMessages.msg("command.transfer_fail"));
                                            return 0;
                                        })))
        );
    }

    private static int place(CommandSourceStack source, LuckyTheme theme, int luck) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        BlockPos pos = player.blockPosition().above();
        ServerLevel level = ServerLevels.of(player);
        level.setBlock(pos, ModBlocks.get(theme).defaultBlockState(), 3);
        if (luck != 0 && level.getBlockEntity(pos) instanceof LuckyBlockEntity be) {
            be.setLuck(luck);
        }
        source.sendSuccess(() -> ModMessages.msg(
                "command.place_ok",
                theme.id(),
                luck != 0 ? ModMessages.msg("command.place_luck_suffix", luck) : Component.empty()
        ), true);
        return 1;
    }

    private static int printStats(CommandSourceStack source, LuckyTheme only) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (only != null) {
            source.sendSuccess(() -> statLine(player, only), false);
            return 1;
        }
        source.sendSuccess(() -> ModMessages.msg("command.stats_header"), false);
        for (LuckyTheme theme : LuckyTheme.values()) {
            source.sendSuccess(() -> statLine(player, theme), false);
        }
        if (PlayerQuestData.hasMissedReroll(player)) {
            source.sendSuccess(() -> ModMessages.msg("command.vip_pending"), false);
        }
        return 1;
    }

    private static Component statLine(ServerPlayer player, LuckyTheme theme) {
        return ModMessages.msg(
                "command.stats_line",
                theme.id(),
                PlayerLuckyBreakStats.breaks(player, theme),
                PlayerLuckyBreakStats.highs(player, theme),
                PlayerLuckyBreakStats.lows(player, theme)
        );
    }

    private static LuckyTheme resolveTheme(String id) throws CommandSyntaxException {
        for (LuckyTheme theme : LuckyTheme.values()) {
            if (theme.id().equalsIgnoreCase(id)) {
                return theme;
            }
        }
        throw UNKNOWN_THEME.create(id);
    }

    private static String suffix(LuckTier tier) {
        return switch (tier) {
            case HIGH -> "high";
            case LOW -> "low";
            case NEUTRAL -> "neutral";
        };
    }
}
