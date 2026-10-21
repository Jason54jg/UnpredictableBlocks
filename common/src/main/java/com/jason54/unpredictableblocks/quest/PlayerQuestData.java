package com.jason54.unpredictableblocks.quest;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import com.jason54.unpredictableblocks.util.ServerLevels;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Compteurs de quêtes et relances VIP par joueur. */
public final class PlayerQuestData {
    private static final long REROLL_TTL_TICKS = 6000;

    private PlayerQuestData() {}

    public static int getBonkCount(ServerPlayer player) {
        return root(player).bonkCounts.getOrDefault(player.getUUID(), 0);
    }

    public static int incrementBonk(ServerPlayer player) {
        QuestSavedData data = root(player);
        int next = data.bonkCounts.getOrDefault(player.getUUID(), 0) + 1;
        data.bonkCounts.put(player.getUUID(), next);
        data.setDirty();
        return next;
    }

    public static int incrementCounter(ServerPlayer player, String key) {
        return addCounter(player, key, 1);
    }

    public static int addCounter(ServerPlayer player, String key, int delta) {
        QuestSavedData data = root(player);
        String ck = counterKey(player.getUUID(), key);
        int next = data.counters.getOrDefault(ck, 0) + delta;
        data.counters.put(ck, next);
        data.setDirty();
        return next;
    }

    public static int getCounter(ServerPlayer player, String key) {
        return root(player).counters.getOrDefault(counterKey(player.getUUID(), key), 0);
    }

    public static void resetCounter(ServerPlayer player, String key) {
        QuestSavedData data = root(player);
        data.counters.remove(counterKey(player.getUUID(), key));
        data.setDirty();
    }

    public static void startSpeedrun(ServerPlayer player) {
        QuestSavedData data = root(player);
        data.speedrunStart.put(player.getUUID(), ServerLevels.of(player).getGameTime());
        data.setDirty();
    }

    public static Optional<Long> speedrunStartTick(ServerPlayer player) {
        Long tick = root(player).speedrunStart.get(player.getUUID());
        return tick == null || tick <= 0 ? Optional.empty() : Optional.of(tick);
    }

    public static void clearSpeedrun(ServerPlayer player) {
        QuestSavedData data = root(player);
        data.speedrunStart.remove(player.getUUID());
        data.setDirty();
    }

    public static void startMultiversTrack(ServerPlayer player, long durationTicks) {
        QuestSavedData data = root(player);
        long now = ServerLevels.of(player).getGameTime();
        data.multiversUntil.put(player.getUUID(), now + durationTicks);
        data.multiversBiomes.remove(player.getUUID());
        data.setDirty();
    }

    public static boolean multiversTracking(ServerPlayer player) {
        QuestSavedData data = root(player);
        Long until = data.multiversUntil.get(player.getUUID());
        if (until == null) {
            return false;
        }
        if (ServerLevels.of(player).getGameTime() > until) {
            data.multiversUntil.remove(player.getUUID());
            data.multiversBiomes.remove(player.getUUID());
            data.setDirty();
            return false;
        }
        return true;
    }

    /** Nombre de biomes distincts enregistrés pendant la fenêtre multivers. */
    public static int recordMultiversBiome(ServerPlayer player, String biomeId) {
        QuestSavedData data = root(player);
        if (!multiversTracking(player)) {
            return 0;
        }
        Set<String> set = data.multiversBiomes.computeIfAbsent(player.getUUID(), u -> new HashSet<>());
        set.add(biomeId);
        data.setDirty();
        return set.size();
    }

    public static void setAnanasQuestFailed(ServerPlayer player, boolean failed) {
        QuestSavedData data = root(player);
        if (failed) {
            data.ananasFailed.add(player.getUUID());
            resetCounter(player, "chef_kill");
        } else {
            data.ananasFailed.remove(player.getUUID());
        }
        data.setDirty();
    }

    public static boolean isAnanasQuestFailed(ServerPlayer player) {
        return root(player).ananasFailed.contains(player.getUUID());
    }

    public static void resetAnanasQuest(ServerPlayer player) {
        setAnanasQuestFailed(player, false);
        resetCounter(player, "chef_kill");
    }

    public static void setFortuneCode(ServerPlayer player, int code, long validTicks) {
        QuestSavedData data = root(player);
        long expire = ServerLevels.of(player).getGameTime() + validTicks;
        data.fortuneCodes.put(player.getUUID(), new FortuneCode(code, expire));
        data.setDirty();
    }

    public static void setEmeraldSnapshot(ServerPlayer player, int count) {
        QuestSavedData data = root(player);
        data.emeraldSnapshot.put(player.getUUID(), count);
        data.setDirty();
    }

    public static int emeraldSnapshot(ServerPlayer player) {
        return root(player).emeraldSnapshot.getOrDefault(player.getUUID(), -1);
    }

    public static void startPlomberieWindow(ServerPlayer player, long durationTicks) {
        QuestSavedData data = root(player);
        data.plomberieUntil.put(player.getUUID(), ServerLevels.of(player).getGameTime() + durationTicks);
        data.setDirty();
    }

    public static boolean plomberieWindowActive(ServerPlayer player) {
        Long until = root(player).plomberieUntil.get(player.getUUID());
        if (until == null) {
            return false;
        }
        if (ServerLevels.of(player).getGameTime() > until) {
            root(player).plomberieUntil.remove(player.getUUID());
            return false;
        }
        return true;
    }

    public static void clearPlomberieWindow(ServerPlayer player) {
        QuestSavedData data = root(player);
        data.plomberieUntil.remove(player.getUUID());
        data.setDirty();
    }

    public static void markGymStrength(ServerPlayer player, long windowTicks) {
        QuestSavedData data = root(player);
        data.gymStrengthUntil.put(player.getUUID(), ServerLevels.of(player).getGameTime() + windowTicks);
        data.setDirty();
    }

    public static boolean hadGymStrengthRecently(ServerPlayer player) {
        Long until = root(player).gymStrengthUntil.get(player.getUUID());
        return until != null && ServerLevels.of(player).getGameTime() <= until;
    }

    public static boolean tryClaimFortuneCode(ServerPlayer player, String message) {
        QuestSavedData data = root(player);
        FortuneCode pending = data.fortuneCodes.get(player.getUUID());
        if (pending == null) {
            return false;
        }
        long now = ServerLevels.of(player).getGameTime();
        if (now > pending.expireTick()) {
            data.fortuneCodes.remove(player.getUUID());
            data.setDirty();
            return false;
        }
        if (!message.contains(String.valueOf(pending.code()))) {
            return false;
        }
        data.fortuneCodes.remove(player.getUUID());
        data.setDirty();
        return true;
    }

    private static String counterKey(UUID uuid, String key) {
        return uuid + ":" + key;
    }

    public static void setMissedReroll(ServerPlayer player, LuckyTheme theme) {
        QuestSavedData data = root(player);
        long expire = ServerLevels.of(player).getGameTime() + REROLL_TTL_TICKS;
        data.missedRerolls.put(player.getUUID(), new MissedReroll(theme.id(), expire));
        data.setDirty();
    }

    public static Optional<LuckyTheme> consumeMissedReroll(ServerPlayer player) {
        QuestSavedData data = root(player);
        MissedReroll missed = data.missedRerolls.get(player.getUUID());
        if (missed == null || ServerLevels.of(player).getGameTime() > missed.expireTick()) {
            data.missedRerolls.remove(player.getUUID());
            data.setDirty();
            return Optional.empty();
        }
        data.missedRerolls.remove(player.getUUID());
        data.setDirty();
        for (LuckyTheme theme : LuckyTheme.values()) {
            if (theme.id().equals(missed.themeId())) {
                return Optional.of(theme);
            }
        }
        return Optional.empty();
    }

    public static boolean hasMissedReroll(ServerPlayer player) {
        MissedReroll missed = root(player).missedRerolls.get(player.getUUID());
        return missed != null && ServerLevels.of(player).getGameTime() <= missed.expireTick();
    }

    public static boolean tryCooldown(ServerPlayer player, String key, long cooldownTicks) {
        QuestSavedData data = root(player);
        long now = ServerLevels.of(player).getGameTime();
        long readyAt = data.cooldowns.getOrDefault(cooldownKey(player.getUUID(), key), 0L);
        if (now < readyAt) {
            return false;
        }
        data.cooldowns.put(cooldownKey(player.getUUID(), key), now + cooldownTicks);
        data.setDirty();
        return true;
    }

    public static long cooldownRemaining(ServerPlayer player, String key) {
        QuestSavedData data = root(player);
        long now = ServerLevels.of(player).getGameTime();
        long readyAt = data.cooldowns.getOrDefault(cooldownKey(player.getUUID(), key), 0L);
        return Math.max(0, readyAt - now);
    }

    private static String cooldownKey(UUID uuid, String key) {
        return uuid + ":" + key;
    }

    private static QuestSavedData root(ServerPlayer player) {
        SavedDataStorage storage = ServerLevels.of(player).getDataStorage();
        return storage.computeIfAbsent(QuestSavedData.TYPE);
    }

    private record MissedReroll(String themeId, long expireTick) {}

    private record FortuneCode(int code, long expireTick) {}

    public static final class QuestSavedData extends SavedData {
        private static final Codec<QuestSavedData> CODEC = CompoundTag.CODEC.xmap(QuestSavedData::fromTag, QuestSavedData::toTag);

        public static final SavedDataType<QuestSavedData> TYPE = new SavedDataType<>(
                Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, "quests"),
                QuestSavedData::new,
                CODEC,
                DataFixTypes.SAVED_DATA_SCOREBOARD
        );

        private final Map<UUID, Integer> bonkCounts = new HashMap<>();
        private final Map<UUID, MissedReroll> missedRerolls = new HashMap<>();
        private final Map<String, Long> cooldowns = new HashMap<>();
        private final Map<String, Integer> counters = new HashMap<>();
        private final Map<UUID, Long> speedrunStart = new HashMap<>();
        private final Map<UUID, Long> multiversUntil = new HashMap<>();
        private final Map<UUID, Set<String>> multiversBiomes = new HashMap<>();
        private final Set<UUID> ananasFailed = new HashSet<>();
        private final Map<UUID, FortuneCode> fortuneCodes = new HashMap<>();
        private final Map<UUID, Long> gymStrengthUntil = new HashMap<>();
        private final Map<UUID, Integer> emeraldSnapshot = new HashMap<>();
        private final Map<UUID, Long> plomberieUntil = new HashMap<>();

        private QuestSavedData() {}

        private static QuestSavedData fromTag(CompoundTag tag) {
            QuestSavedData data = new QuestSavedData();
            CompoundTag bonks = tag.getCompoundOrEmpty("Bonks");
            for (String key : bonks.keySet()) {
                data.bonkCounts.put(UUID.fromString(key), bonks.getIntOr(key, 0));
            }
            CompoundTag missed = tag.getCompoundOrEmpty("MissedRerolls");
            for (String key : missed.keySet()) {
                CompoundTag entry = missed.getCompoundOrEmpty(key);
                data.missedRerolls.put(
                        UUID.fromString(key),
                        new MissedReroll(entry.getStringOr("Theme", ""), entry.getLongOr("Expire", 0L))
                );
            }
            CompoundTag cds = tag.getCompoundOrEmpty("Cooldowns");
            for (String key : cds.keySet()) {
                data.cooldowns.put(key, cds.getLongOr(key, 0L));
            }
            CompoundTag ctr = tag.getCompoundOrEmpty("Counters");
            for (String key : ctr.keySet()) {
                data.counters.put(key, ctr.getIntOr(key, 0));
            }
            CompoundTag speed = tag.getCompoundOrEmpty("SpeedrunStart");
            for (String key : speed.keySet()) {
                data.speedrunStart.put(UUID.fromString(key), speed.getLongOr(key, 0L));
            }
            CompoundTag mvUntil = tag.getCompoundOrEmpty("MultiversUntil");
            for (String key : mvUntil.keySet()) {
                data.multiversUntil.put(UUID.fromString(key), mvUntil.getLongOr(key, 0L));
            }
            CompoundTag mvBiomes = tag.getCompoundOrEmpty("MultiversBiomes");
            for (String key : mvBiomes.keySet()) {
                Set<String> biomes = new HashSet<>();
                for (String b : mvBiomes.getStringOr(key, "").split(",")) {
                    if (!b.isEmpty()) {
                        biomes.add(b);
                    }
                }
                if (!biomes.isEmpty()) {
                    data.multiversBiomes.put(UUID.fromString(key), biomes);
                }
            }
            CompoundTag ananasTag = tag.getCompoundOrEmpty("AnanasFailed");
            for (String key : ananasTag.keySet()) {
                if (ananasTag.getBooleanOr(key, false)) {
                    data.ananasFailed.add(UUID.fromString(key));
                }
            }
            CompoundTag fortune = tag.getCompoundOrEmpty("FortuneCodes");
            for (String key : fortune.keySet()) {
                CompoundTag entry = fortune.getCompoundOrEmpty(key);
                data.fortuneCodes.put(
                        UUID.fromString(key),
                        new FortuneCode(entry.getIntOr("Code", 0), entry.getLongOr("Expire", 0L))
                );
            }
            CompoundTag gym = tag.getCompoundOrEmpty("GymStrengthUntil");
            for (String key : gym.keySet()) {
                data.gymStrengthUntil.put(UUID.fromString(key), gym.getLongOr(key, 0L));
            }
            CompoundTag em = tag.getCompoundOrEmpty("EmeraldSnapshot");
            for (String key : em.keySet()) {
                data.emeraldSnapshot.put(UUID.fromString(key), em.getIntOr(key, 0));
            }
            CompoundTag plomb = tag.getCompoundOrEmpty("PlomberieUntil");
            for (String key : plomb.keySet()) {
                data.plomberieUntil.put(UUID.fromString(key), plomb.getLongOr(key, 0L));
            }
            return data;
        }

        private CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            CompoundTag bonks = new CompoundTag();
            bonkCounts.forEach((uuid, count) -> bonks.putInt(uuid.toString(), count));
            tag.put("Bonks", bonks);

            CompoundTag missed = new CompoundTag();
            missedRerolls.forEach((uuid, reroll) -> {
                CompoundTag entry = new CompoundTag();
                entry.putString("Theme", reroll.themeId());
                entry.putLong("Expire", reroll.expireTick());
                missed.put(uuid.toString(), entry);
            });
            tag.put("MissedRerolls", missed);

            CompoundTag cds = new CompoundTag();
            cooldowns.forEach(cds::putLong);
            tag.put("Cooldowns", cds);

            CompoundTag ctr = new CompoundTag();
            counters.forEach(ctr::putInt);
            tag.put("Counters", ctr);

            CompoundTag speed = new CompoundTag();
            speedrunStart.forEach((uuid, tick) -> speed.putLong(uuid.toString(), tick));
            tag.put("SpeedrunStart", speed);

            CompoundTag mvUntil = new CompoundTag();
            multiversUntil.forEach((uuid, tick) -> mvUntil.putLong(uuid.toString(), tick));
            tag.put("MultiversUntil", mvUntil);

            CompoundTag mvBiomes = new CompoundTag();
            multiversBiomes.forEach((uuid, set) -> mvBiomes.putString(uuid.toString(), String.join(",", set)));
            tag.put("MultiversBiomes", mvBiomes);

            CompoundTag ananas = new CompoundTag();
            ananasFailed.forEach(uuid -> ananas.putBoolean(uuid.toString(), true));
            tag.put("AnanasFailed", ananas);

            CompoundTag fortune = new CompoundTag();
            fortuneCodes.forEach((uuid, fc) -> {
                CompoundTag entry = new CompoundTag();
                entry.putInt("Code", fc.code());
                entry.putLong("Expire", fc.expireTick());
                fortune.put(uuid.toString(), entry);
            });
            tag.put("FortuneCodes", fortune);

            CompoundTag gym = new CompoundTag();
            gymStrengthUntil.forEach((uuid, tick) -> gym.putLong(uuid.toString(), tick));
            tag.put("GymStrengthUntil", gym);

            CompoundTag em = new CompoundTag();
            emeraldSnapshot.forEach((uuid, count) -> em.putInt(uuid.toString(), count));
            tag.put("EmeraldSnapshot", em);

            CompoundTag plomb = new CompoundTag();
            plomberieUntil.forEach((uuid, tick) -> plomb.putLong(uuid.toString(), tick));
            tag.put("PlomberieUntil", plomb);
            return tag;
        }
    }
}
