package com.jason54.unpredictableblocks.quest;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Compteurs de quêtes et relances VIP par joueur. */
public final class PlayerQuestData {
    private static final String DATA_ID = UnpredictableBlocks.MOD_ID + "_quests";
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

    public static void setMissedReroll(ServerPlayer player, LuckyTheme theme) {
        QuestSavedData data = root(player);
        long expire = player.serverLevel().getGameTime() + REROLL_TTL_TICKS;
        data.missedRerolls.put(player.getUUID(), new MissedReroll(theme.id(), expire));
        data.setDirty();
    }

    public static Optional<LuckyTheme> consumeMissedReroll(ServerPlayer player) {
        QuestSavedData data = root(player);
        MissedReroll missed = data.missedRerolls.get(player.getUUID());
        if (missed == null || player.serverLevel().getGameTime() > missed.expireTick()) {
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
        return missed != null && player.serverLevel().getGameTime() <= missed.expireTick();
    }

    public static boolean tryCooldown(ServerPlayer player, String key, long cooldownTicks) {
        QuestSavedData data = root(player);
        long now = player.serverLevel().getGameTime();
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
        long now = player.serverLevel().getGameTime();
        long readyAt = data.cooldowns.getOrDefault(cooldownKey(player.getUUID(), key), 0L);
        return Math.max(0, readyAt - now);
    }

    private static String cooldownKey(UUID uuid, String key) {
        return uuid + ":" + key;
    }

    private static QuestSavedData root(ServerPlayer player) {
        DimensionDataStorage storage = player.serverLevel().getDataStorage();
        return storage.computeIfAbsent(new SavedData.Factory<>(QuestSavedData::new, QuestSavedData::load, null), DATA_ID);
    }

    private record MissedReroll(String themeId, long expireTick) {}

    public static final class QuestSavedData extends SavedData {
        private final Map<UUID, Integer> bonkCounts = new HashMap<>();
        private final Map<UUID, MissedReroll> missedRerolls = new HashMap<>();
        private final Map<String, Long> cooldowns = new HashMap<>();

        static QuestSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
            QuestSavedData data = new QuestSavedData();
            CompoundTag bonks = tag.getCompound("Bonks");
            for (String key : bonks.getAllKeys()) {
                data.bonkCounts.put(UUID.fromString(key), bonks.getInt(key));
            }
            CompoundTag missed = tag.getCompound("MissedRerolls");
            for (String key : missed.getAllKeys()) {
                CompoundTag entry = missed.getCompound(key);
                data.missedRerolls.put(
                        UUID.fromString(key),
                        new MissedReroll(entry.getString("Theme"), entry.getLong("Expire"))
                );
            }
            CompoundTag cds = tag.getCompound("Cooldowns");
            for (String key : cds.getAllKeys()) {
                data.cooldowns.put(key, cds.getLong(key));
            }
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
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
            return tag;
        }
    }
}
