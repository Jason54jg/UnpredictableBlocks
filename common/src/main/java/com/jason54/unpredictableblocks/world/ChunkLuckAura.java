package com.jason54.unpredictableblocks.world;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.config.ModConfig;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

import java.util.HashMap;
import java.util.Map;

/** Après N cassages Lucky dans un chunk : aura bénie (+15) ou maudite (−15) temporaire. */
public final class ChunkLuckAura {
    private static final long AURA_DURATION_TICKS = 20L * 60;

    private ChunkLuckAura() {}

    public static void onLuckyBreak(ServerLevel level, BlockPos origin, ServerPlayer breaker) {
        ChunkAuraData data = get(level);
        String key = chunkKey(level, origin);
        int breaks = data.breakCounts.getOrDefault(key, 0) + 1;
        data.breakCounts.put(key, breaks);
        int threshold = ModConfig.get().meta().chunkAuraThresholdOrDefault();
        if (threshold > 0 && breaks % threshold == 0) {
            boolean blessed = level.getRandom().nextBoolean();
            long until = level.getGameTime() + AURA_DURATION_TICKS;
            data.auras.put(key, new Aura(blessed, until));
            String label = blessed ? "§bChunk béni (+15 Luck)" : "§cChunk maudit (−15 Luck)";
            breaker.sendSystemMessage(Component.literal("§6Aura : " + label + " §7(60s)"));
        }
        data.setDirty();
    }

    public static int luckModifier(ServerLevel level, BlockPos pos) {
        Aura aura = activeAura(level, pos);
        if (aura == null) {
            return 0;
        }
        return aura.blessed ? 15 : -15;
    }

    public static String hudSuffix(ServerLevel level, BlockPos pos) {
        Aura aura = activeAura(level, pos);
        if (aura == null) {
            return "";
        }
        return aura.blessed ? " §b§l✦" : " §c§l☠";
    }

    private static Aura activeAura(ServerLevel level, BlockPos pos) {
        ChunkAuraData data = get(level);
        String key = chunkKey(level, pos);
        Aura aura = data.auras.get(key);
        if (aura == null) {
            return null;
        }
        if (level.getGameTime() > aura.expireTick()) {
            data.auras.remove(key);
            data.setDirty();
            return null;
        }
        return aura;
    }

    private static String chunkKey(ServerLevel level, BlockPos pos) {
        ChunkPos chunk = ChunkPos.containing(pos);
        return level.dimension().identifier() + ":" + chunk.x() + "," + chunk.z();
    }

    private static ChunkAuraData get(ServerLevel level) {
        SavedDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(ChunkAuraData.TYPE);
    }

    private record Aura(boolean blessed, long expireTick) {}

    public static final class ChunkAuraData extends SavedData {
        private static final Codec<ChunkAuraData> CODEC = CompoundTag.CODEC.xmap(ChunkAuraData::fromTag, ChunkAuraData::toTag);

        public static final SavedDataType<ChunkAuraData> TYPE = new SavedDataType<>(
                Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, "chunk_auras"),
                ChunkAuraData::new,
                CODEC,
                DataFixTypes.SAVED_DATA_SCOREBOARD
        );

        private final Map<String, Integer> breakCounts = new HashMap<>();
        private final Map<String, Aura> auras = new HashMap<>();

        private ChunkAuraData() {}

        private static ChunkAuraData fromTag(CompoundTag tag) {
            ChunkAuraData data = new ChunkAuraData();
            CompoundTag breaks = tag.getCompoundOrEmpty("Breaks");
            for (String key : breaks.keySet()) {
                data.breakCounts.put(key, breaks.getIntOr(key, 0));
            }
            CompoundTag auraTag = tag.getCompoundOrEmpty("Auras");
            for (String key : auraTag.keySet()) {
                CompoundTag entry = auraTag.getCompoundOrEmpty(key);
                data.auras.put(
                        key,
                        new Aura(entry.getBooleanOr("Blessed", true), entry.getLongOr("Expire", 0L))
                );
            }
            return data;
        }

        private CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            CompoundTag breaks = new CompoundTag();
            breakCounts.forEach(breaks::putInt);
            tag.put("Breaks", breaks);
            CompoundTag auraTag = new CompoundTag();
            auras.forEach((key, aura) -> {
                CompoundTag entry = new CompoundTag();
                entry.putBoolean("Blessed", aura.blessed());
                entry.putLong("Expire", aura.expireTick());
                auraTag.put(key, entry);
            });
            tag.put("Auras", auraTag);
            return tag;
        }
    }
}
