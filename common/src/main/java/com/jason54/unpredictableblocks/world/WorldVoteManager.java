package com.jason54.unpredictableblocks.world;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.util.ModMessages;
import com.jason54.unpredictableblocks.util.ServerLevels;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Vote lobby avant application d'un profil monde. */
public final class WorldVoteManager {
    private WorldVoteManager() {}

    public static VoteState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(VoteState.TYPE);
    }

    public static void startBallot(ServerLevel level, NextWorldProfileStorage.Pending pending) {
        VoteState vote = get(level);
        vote.active = true;
        vote.pending = pending;
        vote.votes.clear();
        vote.setDirty();
        ParcoursWorldState.get(level).setSessionOpen(false);
        level.getServer().getPlayerList().getPlayers().forEach(p ->
                p.sendSystemMessage(ModMessages.msg("world.vote_started", pending.mode().id(), pending.oneBlockPlayers()))
        );
    }

    public static void cast(ServerPlayer player, String optionKey) {
        ServerLevel level = ServerLevels.of(player);
        VoteState vote = get(level);
        if (!vote.active || vote.pending == null) {
            player.sendSystemMessage(ModMessages.msg("world.vote_inactive"));
            return;
        }
        vote.votes.put(player.getUUID(), optionKey.toLowerCase(Locale.ROOT));
        vote.setDirty();
        player.sendSystemMessage(ModMessages.msg("world.vote_cast", optionKey));
        tryFinalize(level);
    }

    public static void confirm(ServerPlayer initiator) {
        ServerLevel level = ServerLevels.of(initiator);
        VoteState vote = get(level);
        if (!vote.active || vote.pending == null) {
            initiator.sendSystemMessage(ModMessages.msg("world.vote_inactive"));
            return;
        }
        applyPending(level, vote.pending);
        vote.active = false;
        vote.pending = null;
        vote.votes.clear();
        vote.setDirty();
    }

    private static void tryFinalize(ServerLevel level) {
        VoteState vote = get(level);
        int online = level.getServer().getPlayerList().getPlayerCount();
        if (online == 0 || vote.votes.size() < online) {
            return;
        }
        Map<String, Integer> tally = new HashMap<>();
        vote.votes.values().forEach(k -> tally.merge(k, 1, Integer::sum));
        String winner = tally.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("yes");
        if ("yes".equals(winner) || "confirm".equals(winner)) {
            applyPending(level, vote.pending);
            vote.active = false;
            vote.pending = null;
            vote.votes.clear();
            vote.setDirty();
            level.getServer().getPlayerList().getPlayers().forEach(p ->
                    p.sendSystemMessage(ModMessages.msg("world.vote_passed"))
            );
        }
    }

    private static void applyPending(ServerLevel level, NextWorldProfileStorage.Pending pending) {
        WorldModProfile.WorldProfileData profile = WorldModProfile.get(level);
        WorldModProfile.configure(profile, pending.mode(), pending.oneBlockPlayers(), pending.settings());
        WorldProfileApplier.apply(level, profile);
        profile.setConfigured(true);
        profile.setDirty();
        ParcoursWorldState.get(level).setSessionOpen(true);
        ParcoursWorldState.get(level).resetCoopProgress();
        level.getServer().getPlayerList().getPlayers().forEach(player -> {
            player.sendSystemMessage(ModMessages.msg("world.profile_applied", pending.mode().id(), pending.oneBlockPlayers()));
            ModeGameplayHandler.onSessionStart(player, profile);
        });
    }

    public static final class VoteState extends SavedData {
        private static final Codec<VoteState> CODEC = CompoundTag.CODEC.xmap(VoteState::fromTag, VoteState::toTag);

        public static final SavedDataType<VoteState> TYPE = new SavedDataType<>(
                Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, "world_vote"),
                VoteState::new,
                CODEC,
                DataFixTypes.SAVED_DATA_SCOREBOARD
        );

        private boolean active;
        private NextWorldProfileStorage.Pending pending;
        private final Map<UUID, String> votes = new HashMap<>();

        private VoteState() {}

        private static VoteState fromTag(CompoundTag tag) {
            VoteState v = new VoteState();
            v.active = tag.getBooleanOr("Active", false);
            if (tag.contains("PendingMode")) {
                WorldPlayMode.parse(tag.getStringOr("PendingMode", "normal")).ifPresent(mode -> {
                    int players = tag.getIntOr("PendingPlayers", 1);
                    v.pending = new NextWorldProfileStorage.Pending(mode, players, WorldModeSettings.defaults(mode));
                });
            }
            return v;
        }

        private CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("Active", active);
            if (pending != null) {
                tag.putString("PendingMode", pending.mode().id());
                tag.putInt("PendingPlayers", pending.oneBlockPlayers());
            }
            return tag;
        }
    }
}
