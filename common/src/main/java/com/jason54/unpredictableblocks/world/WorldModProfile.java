package com.jason54.unpredictableblocks.world;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Paramètres du mod pour ce monde (persistés dans le save). */
public final class WorldModProfile {
    private WorldModProfile() {}

    public static WorldProfileData get(ServerLevel level) {
        if (!level.dimension().equals(Level.OVERWORLD)) {
            level = level.getServer().overworld();
        }
        return level.getDataStorage().computeIfAbsent(WorldProfileData.TYPE);
    }

    /**
     * Applique le profil « next world » sans construire l'arène (trop lourd au boot NeoForge).
     * La construction se fait au 1er join via {@link #ensureWorldReady}.
     */
    public static void bootstrapOverworld(ServerLevel level) {
        if (!level.dimension().equals(Level.OVERWORLD)) {
            return;
        }
        WorldProfileData data = get(level);
        if (data.configured()) {
            return;
        }
        var pending = NextWorldProfileStorage.peek();
        if (pending.isPresent()) {
            applyPending(data, pending.get());
            data.setConfigured(true);
            data.setDirty();
            NextWorldProfileStorage.clear();
            UnpredictableBlocks.LOGGER.info(
                    "Profil monde en attente appliqué : {} ({} joueurs) — arène au 1er join",
                    data.mode().id(),
                    data.oneBlockPlayers()
            );
            return;
        }
        if (isFlatLike(level)) {
            inferFromLevel(level, data);
            data.setConfigured(true);
            data.setDirty();
            UnpredictableBlocks.LOGGER.info("Profil monde inféré (flat) : {}", data.mode().id());
        }
    }

    /**
     * Construit OneBlock/Parcours si besoin, corrige le spawn, ouvre la session.
     * À appeler quand un joueur est prêt (chunks chargés).
     */
    public static void ensureWorldReady(ServerLevel level) {
        if (!level.dimension().equals(Level.OVERWORLD)) {
            return;
        }
        WorldProfileData data = get(level);
        if (!data.configured()) {
            // Dernière chance : pending encore présent (bootstrap raté).
            bootstrapOverworld(level);
            data = get(level);
            if (!data.configured()) {
                return;
            }
        }
        if (WorldProfileApplier.isArenaMode(data)) {
            try {
                WorldProfileApplier.apply(level, data);
                UnpredictableBlocks.LOGGER.info(
                        "Arène {} prête (built oneblock={} parcours={})",
                        data.mode().id(),
                        data.oneBlockBuilt(),
                        data.parcoursBuilt()
                );
            } catch (Exception e) {
                UnpredictableBlocks.LOGGER.error("Construction arène {} échouée", data.mode().id(), e);
            }
        }
    }

    private static void applyPending(WorldProfileData data, NextWorldProfileStorage.Pending pending) {
        data.setMode(pending.mode());
        data.setOneBlockPlayers(pending.oneBlockPlayers());
        data.setModeSettings(pending.settings());
        tuneForMode(data);
    }

    private static boolean isFlatLike(ServerLevel level) {
        String gen = level.getChunkSource().getGenerator().getClass().getSimpleName();
        return gen.toLowerCase(java.util.Locale.ROOT).contains("flat");
    }

    public static void configure(WorldProfileData data, WorldPlayMode mode, int oneBlockPlayers) {
        configure(data, mode, oneBlockPlayers, WorldModeSettings.defaults(mode));
    }

    public static void configure(WorldProfileData data, WorldPlayMode mode, int oneBlockPlayers, WorldModeSettings settings) {
        data.setMode(mode);
        data.setOneBlockPlayers(Math.clamp(oneBlockPlayers, 1, 4));
        data.setModeSettings(settings == null ? WorldModeSettings.defaults(mode) : settings);
        tuneForMode(data);
        data.setConfigured(true);
    }

    private static void inferFromLevel(ServerLevel level, WorldProfileData data) {
        MinecraftServer server = level.getServer();
        var worldData = server.getWorldData();
        if (isFlatLike(level)) {
            data.setMode(WorldPlayMode.ONEBLOCK);
            data.setOneBlockPlayers(1);
        } else if (worldData.isHardcore()) {
            data.setMode(WorldPlayMode.HARDCORE);
        } else {
            Difficulty diff = worldData.getDifficulty();
            data.setMode(switch (diff) {
                case PEACEFUL, EASY -> WorldPlayMode.EASY;
                case HARD -> WorldPlayMode.HARD;
                default -> WorldPlayMode.NORMAL;
            });
        }
        data.setModeSettings(WorldModeSettings.defaults(data.mode()));
        tuneForMode(data);
    }

    private static void tuneForMode(WorldProfileData data) {
        switch (data.mode()) {
            case EASY -> {
                data.setSpawnMultiplier(1.35);
                data.setDefaultBlockLuck(5);
                data.setNaturalSpawn(true);
            }
            case HARD -> {
                data.setSpawnMultiplier(0.55);
                data.setDefaultBlockLuck(-5);
                data.setNaturalSpawn(true);
            }
            case HARDCORE -> {
                data.setSpawnMultiplier(0.45);
                data.setDefaultBlockLuck(-10);
                data.setNaturalSpawn(true);
            }
            case ONEBLOCK -> {
                data.setSpawnMultiplier(0.0);
                data.setDefaultBlockLuck(15);
                data.setNaturalSpawn(false);
            }
            case PARCOURS -> {
                data.setSpawnMultiplier(0.0);
                data.setDefaultBlockLuck(8);
                data.setNaturalSpawn(false);
            }
            case NORMAL -> {
                data.setSpawnMultiplier(1.0);
                data.setDefaultBlockLuck(0);
                data.setNaturalSpawn(true);
            }
        }
    }

    public static final class WorldProfileData extends SavedData {
        private static final Codec<WorldProfileData> CODEC = CompoundTag.CODEC.xmap(WorldProfileData::fromTag, WorldProfileData::toTag);

        public static final SavedDataType<WorldProfileData> TYPE = new SavedDataType<>(
                Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, "world_profile"),
                WorldProfileData::new,
                CODEC,
                DataFixTypes.SAVED_DATA_SCOREBOARD
        );

        private boolean configured;
        private WorldPlayMode mode = WorldPlayMode.NORMAL;
        private int oneBlockPlayers = 1;
        private double spawnMultiplier = 1.0;
        private int defaultBlockLuck;
        private boolean naturalSpawn = true;
        private boolean oneBlockBuilt;
        private boolean parcoursBuilt;
        private WorldModeSettings modeSettings = WorldModeSettings.defaults(WorldPlayMode.NORMAL);

        private WorldProfileData() {}

        public boolean configured() {
            return configured;
        }

        public void setConfigured(boolean configured) {
            this.configured = configured;
        }

        public WorldPlayMode mode() {
            return mode;
        }

        public void setMode(WorldPlayMode mode) {
            this.mode = mode;
        }

        public int oneBlockPlayers() {
            return oneBlockPlayers;
        }

        public void setOneBlockPlayers(int oneBlockPlayers) {
            this.oneBlockPlayers = oneBlockPlayers;
        }

        public double spawnMultiplier() {
            return spawnMultiplier;
        }

        public void setSpawnMultiplier(double spawnMultiplier) {
            this.spawnMultiplier = spawnMultiplier;
        }

        public int defaultBlockLuck() {
            return defaultBlockLuck;
        }

        public void setDefaultBlockLuck(int defaultBlockLuck) {
            this.defaultBlockLuck = defaultBlockLuck;
        }

        public boolean naturalSpawn() {
            return naturalSpawn;
        }

        public void setNaturalSpawn(boolean naturalSpawn) {
            this.naturalSpawn = naturalSpawn;
        }

        public boolean oneBlockBuilt() {
            return oneBlockBuilt;
        }

        public void setOneBlockBuilt(boolean oneBlockBuilt) {
            this.oneBlockBuilt = oneBlockBuilt;
        }

        public boolean parcoursBuilt() {
            return parcoursBuilt;
        }

        public void setParcoursBuilt(boolean parcoursBuilt) {
            this.parcoursBuilt = parcoursBuilt;
        }

        public WorldModeSettings modeSettings() {
            return modeSettings;
        }

        public void setModeSettings(WorldModeSettings modeSettings) {
            this.modeSettings = modeSettings == null ? WorldModeSettings.defaults(mode) : modeSettings;
        }

        private static WorldProfileData fromTag(CompoundTag tag) {
            WorldProfileData data = new WorldProfileData();
            data.configured = tag.getBooleanOr("Configured", false);
            data.mode = WorldPlayMode.parse(tag.getStringOr("Mode", "normal")).orElse(WorldPlayMode.NORMAL);
            data.oneBlockPlayers = tag.getIntOr("OneBlockPlayers", 1);
            data.spawnMultiplier = tag.getDoubleOr("SpawnMultiplier", 1.0);
            data.defaultBlockLuck = tag.getIntOr("DefaultBlockLuck", 0);
            data.naturalSpawn = tag.getBooleanOr("NaturalSpawn", true);
            data.oneBlockBuilt = tag.getBooleanOr("OneBlockBuilt", false);
            data.parcoursBuilt = tag.getBooleanOr("ParcoursBuilt", false);
            data.modeSettings = new WorldModeSettings(
                    WorldModeSettings.parsePathScale(tag.getStringOr("PathScale", "STANDARD"))
                            .orElse(WorldModeSettings.PathScale.STANDARD),
                    WorldModeSettings.parseDensity(tag.getStringOr("LuckyDensity", "NORMAL"))
                            .orElse(WorldModeSettings.LuckyDensity.NORMAL),
                    WorldModeSettings.parseLayout(tag.getStringOr("ParcoursLayout", "LANES"))
                            .orElse(WorldModeSettings.ParcoursLayout.LANES),
                    WorldModeSettings.parseRules(tag.getStringOr("ParcoursRules", "STANDARD"))
                            .orElse(WorldModeSettings.ParcoursRules.STANDARD),
                    WorldModeSettings.parseDecor(tag.getStringOr("ArenaDecor", "VOID"))
                            .orElse(WorldModeSettings.ArenaDecor.VOID),
                    tag.getBooleanOr("DailySeed", false),
                    tag.getBooleanOr("OneBlockEvolution", data.mode == WorldPlayMode.ONEBLOCK)
            );
            return data;
        }

        private CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("Configured", configured);
            tag.putString("Mode", mode.id());
            tag.putInt("OneBlockPlayers", oneBlockPlayers);
            tag.putDouble("SpawnMultiplier", spawnMultiplier);
            tag.putInt("DefaultBlockLuck", defaultBlockLuck);
            tag.putBoolean("NaturalSpawn", naturalSpawn);
            tag.putBoolean("OneBlockBuilt", oneBlockBuilt);
            tag.putBoolean("ParcoursBuilt", parcoursBuilt);
            tag.putString("PathScale", modeSettings.pathScale().name());
            tag.putString("LuckyDensity", modeSettings.luckyDensity().name());
            tag.putString("ParcoursLayout", modeSettings.parcoursLayout().name());
            tag.putString("ParcoursRules", modeSettings.parcoursRules().name());
            tag.putString("ArenaDecor", modeSettings.arenaDecor().name());
            tag.putBoolean("DailySeed", modeSettings.dailySeed());
            tag.putBoolean("OneBlockEvolution", modeSettings.oneBlockEvolution());
            return tag;
        }
    }
}
