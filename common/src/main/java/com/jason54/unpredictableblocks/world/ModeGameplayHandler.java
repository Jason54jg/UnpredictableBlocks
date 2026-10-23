package com.jason54.unpredictableblocks.world;

import com.jason54.unpredictableblocks.luck.LuckTier;
import com.jason54.unpredictableblocks.luck.PlayerLuckStorage;
import com.jason54.unpredictableblocks.quest.PlayerQuestData;
import com.jason54.unpredictableblocks.quest.QuestRewards;
import com.jason54.unpredictableblocks.theme.LuckyRarity;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import com.jason54.unpredictableblocks.util.ModMessages;
import com.jason54.unpredictableblocks.util.ServerLevels;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Timers, checkpoints, coop, battle spill, oneblock évolution, résumé de fin. */
public final class ModeGameplayHandler {
    /**
     * Délai après join avant construction arène / SafeSpawn.
     * NeoForge gèle si on force des chunks pendant « Loading terrain ».
     */
    private static final int JOIN_SETUP_DELAY_TICKS = 40;
    private static final Map<UUID, Integer> PENDING_JOIN_SETUP = new ConcurrentHashMap<>();

    private ModeGameplayHandler() {}

    public static void register() {
        PlayerEvent.PLAYER_JOIN.register(ModeGameplayHandler::onJoin);
        PlayerEvent.PLAYER_RESPAWN.register((player, conqueredEnd, removalReason) -> onRespawn(player));
        TickEvent.PLAYER_POST.register(ModeGameplayHandler::onPlayerTick);
        TickEvent.SERVER_POST.register(ModeGameplayHandler::drainPendingJoins);
        LifecycleEvent.SERVER_STOPPED.register(server -> PENDING_JOIN_SETUP.clear());
    }

    public static void onSessionStart(ServerPlayer player, WorldModProfile.WorldProfileData profile) {
        if (profile.mode() == WorldPlayMode.PARCOURS && profile.parcoursBuilt()) {
            if (shouldSpectate(player, profile)) {
                player.setGameMode(GameType.SPECTATOR);
                player.sendSystemMessage(ModMessages.msg("world.spectator_until_start"));
            } else {
                WorldProfileApplier.teleportToArena(player, profile);
                int lanes = Math.clamp(profile.oneBlockPlayers(), 1, 4);
                int lane = ParcoursArenaBuilder.laneForPlayer(player.getUUID(), lanes);
                player.sendSystemMessage(ModMessages.msg("world.parcours_lane", lane + 1, lanes));
                resetRun(player);
                startRaceIfNeeded(player, profile);
            }
            return;
        }
        if (profile.mode() == WorldPlayMode.ONEBLOCK && profile.oneBlockBuilt()) {
            WorldProfileApplier.teleportToArena(player, profile);
        }
    }

    private static void onJoin(net.minecraft.world.entity.player.Player player) {
        if (player.level().isClientSide() || !(player instanceof ServerPlayer sp)) {
            return;
        }
        // Spectateur temporaire en mode arène : évite la chute dans le vide pendant le délai NeoForge.
        ServerLevel overworld = ServerLevels.server(sp).overworld();
        WorldModProfile.bootstrapOverworld(overworld);
        WorldModProfile.WorldProfileData preview = WorldModProfile.get(overworld);
        if (WorldProfileApplier.isArenaMode(preview)) {
            sp.setGameMode(GameType.SPECTATOR);
        }
        PENDING_JOIN_SETUP.put(sp.getUUID(), JOIN_SETUP_DELAY_TICKS);
    }

    private static void drainPendingJoins(net.minecraft.server.MinecraftServer server) {
        if (PENDING_JOIN_SETUP.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<UUID, Integer>> it = PENDING_JOIN_SETUP.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();
            int left = entry.getValue() - 1;
            if (left > 0) {
                entry.setValue(left);
                continue;
            }
            it.remove();
            ServerPlayer sp = server.getPlayerList().getPlayer(entry.getKey());
            if (sp == null || sp.isRemoved()) {
                continue;
            }
            finishJoinSetup(sp);
        }
    }

    private static void finishJoinSetup(ServerPlayer sp) {
        ServerLevel overworld = ServerLevels.server(sp).overworld();
        WorldModProfile.ensureWorldReady(overworld);
        WorldModProfile.WorldProfileData p = WorldModProfile.get(overworld);
        if (!p.configured()) {
            return;
        }
        if (WorldProfileApplier.isArenaMode(p)
                && ((p.mode() == WorldPlayMode.PARCOURS && p.parcoursBuilt())
                || (p.mode() == WorldPlayMode.ONEBLOCK && p.oneBlockBuilt()))) {
            if (!shouldSpectate(sp, p)) {
                sp.setGameMode(GameType.SURVIVAL);
            }
            onSessionStart(sp, p);
            return;
        }
        if (sp.gameMode.getGameModeForPlayer() == GameType.SPECTATOR
                && ServerLevels.server(sp).isSingleplayer()) {
            // Restaure survie si on avait forcé le spectateur pour une arène annulée.
            sp.setGameMode(GameType.SURVIVAL);
        }
        // Map normale / easy / hard : remonte si spawn vanilla dans la terre.
        SafeSpawn.rescueIfBuried(sp);
    }

    private static void onRespawn(ServerPlayer player) {
        if (player.level().isClientSide()) {
            return;
        }
        ServerLevel overworld = ServerLevels.server(player).overworld();
        WorldModProfile.WorldProfileData profile = WorldModProfile.get(overworld);
        if (!profile.configured()) {
            return;
        }
        ServerLevels.server(player).execute(() -> {
            if (player.isRemoved()) {
                return;
            }
            WorldModProfile.WorldProfileData p = WorldModProfile.get(overworld);
            if (p.mode() == WorldPlayMode.ONEBLOCK && p.oneBlockBuilt()) {
                WorldProfileApplier.teleportToArena(player, p);
                return;
            }
            if (p.mode() == WorldPlayMode.PARCOURS && p.parcoursBuilt()) {
                WorldProfileApplier.teleportToArena(player, p);
                return;
            }
            SafeSpawn.rescueIfBuried(player);
        });
    }

    private static boolean shouldSpectate(ServerPlayer player, WorldModProfile.WorldProfileData profile) {
        int lanes = Math.clamp(profile.oneBlockPlayers(), 1, 4);
        var online = ServerLevels.server(player).getPlayerList().getPlayers();
        int index = online.indexOf(player);
        return index >= lanes;
    }

    public static void afterLuckyBreak(
            ServerLevel level,
            ServerPlayer player,
            BlockPos pos,
            LuckyTheme theme,
            LuckTier tier
    ) {
        WorldModProfile.WorldProfileData profile = WorldModProfile.get(level);
        if (profile.mode() == WorldPlayMode.ONEBLOCK) {
            OneBlockArenaBuilder.scheduleRespawn(level, profile);
            if (profile.modeSettings().oneBlockEvolution()) {
                handleOneBlockEvolution(level, profile, player);
            }
            return;
        }
        if (profile.mode() != WorldPlayMode.PARCOURS) {
            return;
        }
        PlayerQuestData.addCounter(player, "parcours_breaks", 1);
        if (theme.rarity() == LuckyRarity.LEGENDARY) {
            PlayerQuestData.addCounter(player, "parcours_legendary", 1);
        }
        if (tier == LuckTier.HIGH) {
            PlayerQuestData.addCounter(player, "parcours_luck_gain", 3);
        } else if (tier == LuckTier.LOW) {
            PlayerQuestData.addCounter(player, "parcours_luck_loss", 2);
        }
        WorldModeSettings settings = profile.modeSettings();
        if (settings.parcoursRules() == WorldModeSettings.ParcoursRules.RACE && tier == LuckTier.HIGH) {
            PlayerQuestData.addCounter(player, "parcours_time_bonus", 40);
        } else if (settings.parcoursRules() == WorldModeSettings.ParcoursRules.RACE && tier == LuckTier.LOW) {
            PlayerQuestData.addCounter(player, "parcours_time_penalty", 60);
        }
        if (settings.parcoursLayout() == WorldModeSettings.ParcoursLayout.COOP) {
            int required = Math.max(1, profile.oneBlockPlayers());
            ParcoursWorldState.get(level).recordCoopBreak(required);
            ParcoursArenaBuilder.syncCoopGates(level, profile, WorldProfileApplier.arenaOrigin(profile, new BlockPos(0, WorldProfileApplier.ARENA_Y, 0)));
        }
        if (settings.parcoursLayout() == WorldModeSettings.ParcoursLayout.BATTLE) {
            spillBattleEffect(level, player, pos, tier, profile);
        }
        tryFinish(level, player, profile);
    }

    private static void handleOneBlockEvolution(
            ServerLevel level,
            WorldModProfile.WorldProfileData profile,
            ServerPlayer player
    ) {
        int breaks = PlayerQuestData.incrementCounter(player, "oneblock_evolution_breaks");
        BlockPos center = player.blockPosition();
        if (breaks == 50) {
            OneBlockArenaBuilder.expandPlatform(level, center, 2);
            player.sendSystemMessage(ModMessages.msg("world.oneblock_phase2"));
        } else if (breaks == 100) {
            ParcoursArenaBuilder.buildMiniBridge(level, center, profile);
            player.sendSystemMessage(ModMessages.msg("world.oneblock_phase3"));
            QuestRewards.complete(player, "mode/oneblock_evolution", "evolved");
        }
    }

    private static void spillBattleEffect(
            ServerLevel level,
            ServerPlayer source,
            BlockPos pos,
            LuckTier tier,
            WorldModProfile.WorldProfileData profile
    ) {
        int modifier = tier == LuckTier.HIGH ? 3 : (tier == LuckTier.LOW ? -3 : 0);
        if (modifier == 0) {
            return;
        }
        for (ServerPlayer other : level.getServer().getPlayerList().getPlayers()) {
            if (other == source || other.level().dimension() != Level.OVERWORLD) {
                continue;
            }
            if (other.blockPosition().distSqr(pos) <= 25) {
                PlayerLuckStorage.addModifier(other, modifier);
                other.sendSystemMessage(ModMessages.msg("world.battle_spill", source.getName().getString(), modifier));
            }
        }
    }

    private static void onPlayerTick(net.minecraft.world.entity.player.Player player) {
        if (player.level().isClientSide() || !(player instanceof ServerPlayer sp)) {
            return;
        }
        ServerLevel level = ServerLevels.of(sp);
        WorldModProfile.WorldProfileData profile = WorldModProfile.get(level);
        if (profile.mode() != WorldPlayMode.PARCOURS || !profile.parcoursBuilt()) {
            return;
        }
        if (PlayerQuestData.getCounter(sp, "parcours_finished") > 0) {
            return;
        }
        WorldModeSettings settings = profile.modeSettings();
        if (settings.parcoursRules() == WorldModeSettings.ParcoursRules.HARDCORE) {
            handleHardcoreFall(sp, profile);
        }
        updateCheckpoint(sp, profile);
        tryFinish(level, sp, profile);
    }

    private static void handleHardcoreFall(ServerPlayer player, WorldModProfile.WorldProfileData profile) {
        BlockPos origin = WorldProfileApplier.arenaOrigin(profile, player.blockPosition());
        int lane = effectiveLane(player, profile);
        int lanes = laneCount(profile);
        if (player.getY() < origin.getY() - 6) {
            BlockPos cp = ParcoursArenaBuilder.checkpointPos(origin, lane, lanes, profile, checkpointStep(player));
            player.teleportTo(
                    ServerLevels.server(player).overworld(),
                    cp.getX() + 0.5,
                    cp.getY(),
                    cp.getZ() + 0.5,
                    java.util.Set.of(),
                    player.getYRot(),
                    player.getXRot(),
                    true
            );
            player.setDeltaMovement(Vec3.ZERO);
            player.sendSystemMessage(ModMessages.msg("world.checkpoint_reset"));
        }
    }

    private static void updateCheckpoint(ServerPlayer player, WorldModProfile.WorldProfileData profile) {
        BlockPos origin = WorldProfileApplier.arenaOrigin(profile, player.blockPosition());
        int lane = effectiveLane(player, profile);
        int lanes = laneCount(profile);
        int step = ParcoursArenaBuilder.stepAt(origin, lane, lanes, player.blockPosition());
        int prev = PlayerQuestData.getCounter(player, "parcours_checkpoint_step");
        if (step >= prev + 12) {
            PlayerQuestData.resetCounter(player, "parcours_checkpoint_step");
            PlayerQuestData.addCounter(player, "parcours_checkpoint_step", step);
            player.sendSystemMessage(ModMessages.msg("world.checkpoint_saved", step));
        }
    }

    private static int checkpointStep(ServerPlayer player) {
        return Math.max(0, PlayerQuestData.getCounter(player, "parcours_checkpoint_step"));
    }

    private static void tryFinish(ServerLevel level, ServerPlayer player, WorldModProfile.WorldProfileData profile) {
        if (PlayerQuestData.getCounter(player, "parcours_finished") > 0) {
            return;
        }
        BlockPos origin = WorldProfileApplier.arenaOrigin(profile, player.blockPosition());
        int lane = effectiveLane(player, profile);
        int lanes = laneCount(profile);
        if (!ParcoursArenaBuilder.isAtFinish(origin, lane, lanes, profile, player.blockPosition())) {
            return;
        }
        PlayerQuestData.addCounter(player, "parcours_finished", 1);
        long elapsed = raceElapsedTicks(player, profile);
        awardMedal(player, elapsed, profile);
        checkWeeklyChallenge(player);
        sendSummary(player, elapsed);
        if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
            player.setGameMode(GameType.SURVIVAL);
        }
    }

    private static void awardMedal(ServerPlayer player, long elapsedTicks, WorldModProfile.WorldProfileData profile) {
        QuestRewards.complete(player, "mode/parcours_bronze", "finish");
        if (profile.modeSettings().parcoursRules() != WorldModeSettings.ParcoursRules.RACE) {
            return;
        }
        long bonus = PlayerQuestData.getCounter(player, "parcours_time_bonus");
        long penalty = PlayerQuestData.getCounter(player, "parcours_time_penalty");
        long adjusted = Math.max(0, elapsedTicks - bonus + penalty);
        if (adjusted <= 60 * 20L) {
            QuestRewards.complete(player, "mode/parcours_gold", "gold");
            player.sendSystemMessage(ModMessages.msg("world.medal_gold"));
        } else if (adjusted <= 120 * 20L) {
            QuestRewards.complete(player, "mode/parcours_silver", "silver");
            player.sendSystemMessage(ModMessages.msg("world.medal_silver"));
        } else {
            player.sendSystemMessage(ModMessages.msg("world.medal_bronze"));
        }
    }

    private static void checkWeeklyChallenge(ServerPlayer player) {
        int week = LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        int stored = PlayerQuestData.getCounter(player, "weekly_challenge_week");
        if (stored != week) {
            PlayerQuestData.resetCounter(player, "parcours_legendary");
            PlayerQuestData.addCounter(player, "weekly_challenge_week", week);
        }
        if (PlayerQuestData.getCounter(player, "parcours_legendary") == 0) {
            QuestRewards.complete(player, "mode/weekly_clean", "clean");
            PlayerLuckStorage.addModifier(player, 10);
            player.sendSystemMessage(ModMessages.msg("world.weekly_clean"));
        }
    }

    private static void sendSummary(ServerPlayer player, long elapsedTicks) {
        int breaks = PlayerQuestData.getCounter(player, "parcours_breaks");
        int gain = PlayerQuestData.getCounter(player, "parcours_luck_gain");
        int loss = PlayerQuestData.getCounter(player, "parcours_luck_loss");
        double seconds = elapsedTicks / 20.0;
        player.sendSystemMessage(ModMessages.msg("world.run_summary", String.format(Locale.ROOT, "%.1f", seconds), breaks, gain, loss));
    }

    private static void resetRun(ServerPlayer player) {
        PlayerQuestData.resetCounter(player, "parcours_finished");
        PlayerQuestData.resetCounter(player, "parcours_breaks");
        PlayerQuestData.resetCounter(player, "parcours_legendary");
        PlayerQuestData.resetCounter(player, "parcours_luck_gain");
        PlayerQuestData.resetCounter(player, "parcours_luck_loss");
        PlayerQuestData.resetCounter(player, "parcours_time_bonus");
        PlayerQuestData.resetCounter(player, "parcours_time_penalty");
        PlayerQuestData.resetCounter(player, "parcours_checkpoint_step");
        PlayerQuestData.clearSpeedrun(player);
    }

    private static void startRaceIfNeeded(ServerPlayer player, WorldModProfile.WorldProfileData profile) {
        if (profile.modeSettings().parcoursRules() == WorldModeSettings.ParcoursRules.RACE) {
            PlayerQuestData.startSpeedrun(player);
            player.sendSystemMessage(ModMessages.msg("world.race_start"));
        }
    }

    private static long raceElapsedTicks(ServerPlayer player, WorldModProfile.WorldProfileData profile) {
        if (profile.modeSettings().parcoursRules() != WorldModeSettings.ParcoursRules.RACE) {
            return 0;
        }
        return PlayerQuestData.speedrunStartTick(player)
                .map(start -> ServerLevels.of(player).getGameTime() - start)
                .orElse(0L);
    }

    private static int effectiveLane(ServerPlayer player, WorldModProfile.WorldProfileData profile) {
        if (profile.modeSettings().parcoursLayout() == WorldModeSettings.ParcoursLayout.COOP) {
            return 0;
        }
        return ParcoursArenaBuilder.laneForPlayer(player.getUUID(), laneCount(profile));
    }

    private static int laneCount(WorldModProfile.WorldProfileData profile) {
        return Math.clamp(profile.oneBlockPlayers(), 1, 4);
    }
}
