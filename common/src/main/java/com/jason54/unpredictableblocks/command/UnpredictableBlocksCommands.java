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
            new DynamicCommandExceptionType(id -> Component.literal("Thème inconnu : " + id));

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
                                                    ctx.getSource().sendSuccess(() -> Component.literal("Événement " + theme.id() + " / " + tier), true);
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
                                            () -> Component.literal("Config unpredictableblocks.json rechargée."),
                                            true
                                    );
                                    return 1;
                                }))
                        .then(Commands.literal("gameplay")
                                .executes(ctx -> {
                                    var g = ModConfig.gameplay();
                                    ctx.getSource().sendSuccess(() -> Component.literal("§6Gameplay (config) :"), false);
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            "§7carte HIGH §f" + (int) (g.cardDropOnHighOrDefault() * 100) + "%"
                                                    + " §7| combo §f" + g.comboHitsRequiredOrDefault()
                                                    + "× / " + (g.comboWindowTicksOrDefault() / 20) + "s"
                                    ), false);
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            "§7near miss luck §f[" + g.nearMissLuckMinOrDefault()
                                                    + "…" + g.nearMissLuckMaxOrDefault() + "]"
                                                    + " §7| maître §f" + g.themeMasterBreaksOrDefault() + " breaks"
                                    ), false);
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            "§7série malus §f" + g.malusStreakLengthOrDefault()
                                                    + " LOW → +" + g.malusStreakLuckBonusOrDefault() + " luck"
                                                    + " §7| VIP TTL §f" + (g.missedRerollTicksOrDefault() / 20) + "s"
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
                        .then(Commands.literal("transfer")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer from = ctx.getSource().getPlayerOrException();
                                            ServerPlayer to = EntityArgument.getPlayer(ctx, "target");
                                            if (from.getUUID().equals(to.getUUID())) {
                                                ctx.getSource().sendFailure(Component.literal("Cible invalide."));
                                                return 0;
                                            }
                                            if (!PlayerQuestData.hasMissedReroll(from)) {
                                                ctx.getSource().sendFailure(Component.literal("Aucun malus en attente à transférer."));
                                                return 0;
                                            }
                                            if (PlayerQuestData.transferMissedReroll(from, to)) {
                                                ctx.getSource().sendSuccess(
                                                        () -> Component.literal("Malus VIP transféré à " + to.getGameProfile().name()),
                                                        true
                                                );
                                                to.sendSystemMessage(Component.literal(
                                                        "§e" + from.getGameProfile().name() + " §7vous a transféré un §6relance VIP§7 (malus en attente)."
                                                ));
                                                return 1;
                                            }
                                            ctx.getSource().sendFailure(Component.literal("Transfert impossible (expiré ?)."));
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
        source.sendSuccess(() -> Component.literal("Placé " + theme.id() + (luck != 0 ? " luck=" + luck : "")), true);
        return 1;
    }

    private static int printStats(CommandSourceStack source, LuckyTheme only) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (only != null) {
            source.sendSuccess(() -> statLine(player, only), false);
            return 1;
        }
        source.sendSuccess(() -> Component.literal("§6Stats cassages Lucky (HIGH/LOW) :"), false);
        for (LuckyTheme theme : LuckyTheme.values()) {
            source.sendSuccess(() -> statLine(player, theme), false);
        }
        if (PlayerQuestData.hasMissedReroll(player)) {
            source.sendSuccess(() -> Component.literal("§eMalus VIP en attente : oui"), false);
        }
        return 1;
    }

    private static Component statLine(ServerPlayer player, LuckyTheme theme) {
        return Component.literal(
                "§7" + theme.id() + " §8— breaks "
                        + PlayerLuckyBreakStats.breaks(player, theme)
                        + " §aH" + PlayerLuckyBreakStats.highs(player, theme)
                        + " §cL" + PlayerLuckyBreakStats.lows(player, theme)
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
