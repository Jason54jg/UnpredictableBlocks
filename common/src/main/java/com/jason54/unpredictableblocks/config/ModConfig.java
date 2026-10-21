package com.jason54.unpredictableblocks.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.theme.LuckyRarity;
import com.jason54.unpredictableblocks.theme.LuckyTheme;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static UnpredictableBlocksConfig ROOT = UnpredictableBlocksConfig.defaults();

    private ModConfig() {}

    public static UnpredictableBlocksConfig get() {
        return ROOT;
    }

    public static ThemeSpawn themeSpawn(LuckyTheme theme) {
        ThemeSpawn fromFile = get().themes().get(theme.id());
        return fromFile != null ? fromFile : ThemeSpawn.forTheme(theme);
    }

    public static Gameplay gameplay() {
        Gameplay fromFile = get().gameplay();
        return fromFile != null ? fromFile : Gameplay.defaults();
    }

    public static void load() {
        Path path = PlatformConfigPaths.configFile("unpredictableblocks.json");
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                ROOT = GSON.fromJson(reader, UnpredictableBlocksConfig.class);
                if (ROOT == null) {
                    ROOT = UnpredictableBlocksConfig.defaults();
                }
            } catch (IOException e) {
                UnpredictableBlocks.LOGGER.error("Impossible de lire unpredictableblocks.json", e);
                ROOT = UnpredictableBlocksConfig.defaults();
            }
        } else {
            ROOT = UnpredictableBlocksConfig.defaults();
            try {
                Files.createDirectories(path.getParent());
                Files.writeString(path, GSON.toJson(ROOT));
            } catch (IOException e) {
                UnpredictableBlocks.LOGGER.warn("Écriture config par défaut impossible", e);
            }
        }
    }

    public record UnpredictableBlocksConfig(
            Meta meta,
            Spawn spawn,
            Map<String, ThemeSpawn> themes,
            Map<String, String> lootHighOverrides,
            Gameplay gameplay
    ) {
        static UnpredictableBlocksConfig defaults() {
            Map<String, ThemeSpawn> themes = new HashMap<>();
            for (LuckyTheme theme : LuckyTheme.values()) {
                themes.put(theme.id(), ThemeSpawn.forTheme(theme));
            }
            return new UnpredictableBlocksConfig(
                    new Meta("26.1.2,26.2,26.3", 0, null, null),
                    Spawn.defaults(),
                    themes,
                    new HashMap<>(),
                    Gameplay.defaults()
            );
        }
    }

    /** Réglages meta-game (combo, cartes, near miss, maître thème, VIP). */
    public record Gameplay(
            Double cardDropOnHigh,
            Integer comboWindowSeconds,
            Integer comboHitsRequired,
            Integer nearMissLuckMin,
            Integer nearMissLuckMax,
            Integer malusStreakLength,
            Integer malusStreakLuckBonus,
            Integer themeMasterBreaks,
            Integer missedRerollSeconds,
            Integer luckBetAmount,
            Double ambientParticleRadius,
            Integer ambientParticleIntervalTicks,
            Boolean useModTierSounds
    ) {
        static Gameplay defaults() {
            return new Gameplay(0.12, 120, 3, -58, -50, 3, 5, 10, 300, 25, 6.0, 15, true);
        }

        public double cardDropOnHighOrDefault() {
            if (cardDropOnHigh == null) {
                return 0.12;
            }
            return Math.clamp(cardDropOnHigh, 0.0, 1.0);
        }

        public long comboWindowTicksOrDefault() {
            int seconds = comboWindowSeconds == null ? 120 : Math.max(5, comboWindowSeconds);
            return seconds * 20L;
        }

        public int comboHitsRequiredOrDefault() {
            return comboHitsRequired == null ? 3 : Math.max(2, comboHitsRequired);
        }

        public int nearMissLuckMinOrDefault() {
            return nearMissLuckMin == null ? -58 : nearMissLuckMin;
        }

        public int nearMissLuckMaxOrDefault() {
            return nearMissLuckMax == null ? -50 : nearMissLuckMax;
        }

        public int malusStreakLengthOrDefault() {
            return malusStreakLength == null ? 3 : Math.max(1, malusStreakLength);
        }

        public int malusStreakLuckBonusOrDefault() {
            return malusStreakLuckBonus == null ? 5 : malusStreakLuckBonus;
        }

        public int themeMasterBreaksOrDefault() {
            return themeMasterBreaks == null ? 10 : Math.max(1, themeMasterBreaks);
        }

        public long missedRerollTicksOrDefault() {
            int seconds = missedRerollSeconds == null ? 300 : Math.max(30, missedRerollSeconds);
            return seconds * 20L;
        }

        public int luckBetAmountOrDefault() {
            return luckBetAmount == null ? 25 : Math.clamp(luckBetAmount, 1, 100);
        }

        public double ambientParticleRadiusOrDefault() {
            return ambientParticleRadius == null ? 6.0 : Math.clamp(ambientParticleRadius, 2.0, 16.0);
        }

        public int ambientParticleIntervalTicksOrDefault() {
            return ambientParticleIntervalTicks == null ? 15 : Math.clamp(ambientParticleIntervalTicks, 5, 100);
        }

        public boolean useModTierSoundsOrDefault() {
            return useModTierSounds == null || useModTierSounds;
        }
    }

    public record Meta(String supportedVersions, int defaultBlockLuck, Boolean showLuckHud, Integer chunkAuraThreshold) {
        public boolean showLuckHudOrDefault() {
            return showLuckHud == null || showLuckHud;
        }

        /** Cassages Lucky par chunk avant aura ; 0 désactive. */
        public int chunkAuraThresholdOrDefault() {
            return chunkAuraThreshold == null ? 5 : Math.max(0, chunkAuraThreshold);
        }
    }

    public record Spawn(
            int attemptsPerChunk,
            EnumMap<LuckyRarity, Integer> rarityWeights,
            DimensionWeights dimensions
    ) {
        static Spawn defaults() {
            EnumMap<LuckyRarity, Integer> weights = new EnumMap<>(LuckyRarity.class);
            for (LuckyRarity r : LuckyRarity.values()) {
                weights.put(r, r.defaultWeight());
            }
            return new Spawn(
                    2,
                    weights,
                    new DimensionWeights(1.0, 0.35, 0.15)
            );
        }
    }

    public record DimensionWeights(double overworld, double nether, double end) {}

    public record ThemeSpawn(
            LuckyRarity rarity,
            double overworldWeight,
            double netherWeight,
            double endWeight,
            String[] preferredBiomes,
            boolean nightOnly
    ) {
        public static ThemeSpawn forTheme(LuckyTheme theme) {
            return switch (theme) {
                case ANACHRONISME -> new ThemeSpawn(LuckyRarity.RARE, 1.2, 0.4, 0.2, new String[]{"plains", "badlands"}, false);
                case BOULANGERIE -> new ThemeSpawn(LuckyRarity.COMMON, 1.5, 0.2, 0.1, new String[]{"*"}, false);
                case BSOD -> new ThemeSpawn(LuckyRarity.LEGENDARY, 0.4, 0.4, 0.4, new String[]{"*"}, false);
                case VOLAILLE -> new ThemeSpawn(LuckyRarity.COMMON, 1.0, 0.3, 0.1, new String[]{"jungle", "forest"}, false);
                case IMPOTS -> new ThemeSpawn(LuckyRarity.RARE, 1.3, 0.1, 0.0, new String[]{"village"}, false);
                case STREAMER -> new ThemeSpawn(LuckyRarity.RARE, 1.1, 0.2, 0.1, new String[]{"beach", "ocean"}, false);
                case PROPRETE -> new ThemeSpawn(LuckyRarity.COMMON, 1.0, 0.2, 0.1, new String[]{"river", "snowy"}, false);
                case GRAVITE_ZERO -> new ThemeSpawn(LuckyRarity.LEGENDARY, 0.3, 0.2, 1.5, new String[]{"end_highlands", "peaks"}, false);
                case FAST_FOOD -> new ThemeSpawn(LuckyRarity.COMMON, 1.2, 0.3, 0.1, new String[]{"plains", "savanna"}, false);
                case CAUCHEMAR_MATIN -> new ThemeSpawn(LuckyRarity.RARE, 1.0, 0.2, 0.2, new String[]{"*"}, true);
                case FILM_ACTION -> new ThemeSpawn(LuckyRarity.RARE, 1.0, 0.5, 0.1, new String[]{"badlands", "desert"}, false);
                case JDR_D20 -> new ThemeSpawn(LuckyRarity.RARE, 0.9, 0.6, 0.3, new String[]{"dark_forest", "taiga"}, false);
                case PENURIE_2020 -> new ThemeSpawn(LuckyRarity.COMMON, 1.1, 0.1, 0.0, new String[]{"village", "beach"}, false);
                case COMPLOT -> new ThemeSpawn(LuckyRarity.LEGENDARY, 0.5, 0.3, 0.2, new String[]{"desert", "swamp"}, false);
                case LAG_BUG -> new ThemeSpawn(LuckyRarity.LEGENDARY, 0.6, 0.6, 0.6, new String[]{"*"}, false);
                case DISCO_80 -> new ThemeSpawn(LuckyRarity.RARE, 1.0, 0.3, 0.2, new String[]{"*"}, true);
                case PLOMBERIE -> new ThemeSpawn(LuckyRarity.COMMON, 1.0, 0.2, 0.1, new String[]{"swamp", "river", "dripstone"}, false);
                case FORTUNE_COOKIE -> new ThemeSpawn(LuckyRarity.RARE, 0.8, 0.2, 0.1, new String[]{"jungle", "bamboo"}, false);
                case CAMPING -> new ThemeSpawn(LuckyRarity.COMMON, 1.1, 0.2, 0.1, new String[]{"taiga", "forest", "peaks"}, false);
                case GYM_BRO -> new ThemeSpawn(LuckyRarity.RARE, 1.0, 0.4, 0.1, new String[]{"plains", "desert"}, false);
                case ART_MODERNE -> new ThemeSpawn(LuckyRarity.RARE, 1.0, 0.1, 0.1, new String[]{"village", "badlands"}, false);
                case MAGIE_COMPTOIR -> new ThemeSpawn(LuckyRarity.COMMON, 0.9, 0.5, 0.2, new String[]{"swamp", "mansion"}, false);
                case CASINO -> new ThemeSpawn(LuckyRarity.LEGENDARY, 0.4, 0.6, 0.1, new String[]{"desert", "badlands"}, false);
                case INVASION_MEMES -> new ThemeSpawn(LuckyRarity.LEGENDARY, 0.5, 0.5, 0.5, new String[]{"*"}, false);
                case PAUSE_CAFE -> new ThemeSpawn(LuckyRarity.COMMON, 1.3, 0.1, 0.0, new String[]{"plains", "village"}, false);
                case RETARD_VOL -> new ThemeSpawn(LuckyRarity.RARE, 1.0, 0.2, 0.1, new String[]{"plains", "desert"}, false);
                case ANANAS_PIZZA -> new ThemeSpawn(LuckyRarity.RARE, 1.1, 0.2, 0.0, new String[]{"jungle", "savanna"}, false);
                case SPEEDRUNNER -> new ThemeSpawn(LuckyRarity.LEGENDARY, 0.3, 0.8, 0.6, new String[]{"stronghold", "nether_wastes"}, false);
                case PIRATERIE_TORRENT -> new ThemeSpawn(LuckyRarity.RARE, 0.8, 0.2, 0.1, new String[]{"ocean", "beach"}, false);
                case ASMR_FIDGET -> new ThemeSpawn(LuckyRarity.COMMON, 1.2, 0.2, 0.2, new String[]{"*"}, false);
                case VAMPIRE_GOTHIQUE -> new ThemeSpawn(LuckyRarity.RARE, 1.0, 0.3, 0.1, new String[]{"dark_forest", "roofed_forest"}, true);
                case MULTIVERS_SLIDERS -> new ThemeSpawn(LuckyRarity.LEGENDARY, 0.2, 0.7, 1.2, new String[]{"end_highlands", "nether_wastes"}, false);
            };
        }
    }
}
