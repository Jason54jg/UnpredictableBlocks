package com.jason54.wtfluckyblocks.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jason54.wtfluckyblocks.WtfLuckyBlocks;
import com.jason54.wtfluckyblocks.theme.LuckyRarity;
import com.jason54.wtfluckyblocks.theme.LuckyTheme;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static WtfConfig ROOT = WtfConfig.defaults();

    private ModConfig() {}

    public static WtfConfig get() {
        return ROOT;
    }

    public static void load() {
        Path path = PlatformConfigPaths.configFile("wtf_luckyblocks.json");
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                ROOT = GSON.fromJson(reader, WtfConfig.class);
                if (ROOT == null) {
                    ROOT = WtfConfig.defaults();
                }
            } catch (IOException e) {
                WtfLuckyBlocks.LOGGER.error("Impossible de lire wtf_luckyblocks.json", e);
                ROOT = WtfConfig.defaults();
            }
        } else {
            ROOT = WtfConfig.defaults();
            try {
                Files.createDirectories(path.getParent());
                Files.writeString(path, GSON.toJson(ROOT));
            } catch (IOException e) {
                WtfLuckyBlocks.LOGGER.warn("Écriture config par défaut impossible", e);
            }
        }
    }

    public record WtfConfig(Meta meta, Spawn spawn, Map<String, ThemeSpawn> themes) {
        static WtfConfig defaults() {
            Map<String, ThemeSpawn> themes = new HashMap<>();
            for (LuckyTheme theme : LuckyTheme.values()) {
                themes.put(theme.id(), ThemeSpawn.forTheme(theme));
            }
            return new WtfConfig(
                    new Meta("26.1.2,26.2,26.3", 0),
                    Spawn.defaults(),
                    themes
            );
        }
    }

    public record Meta(String supportedVersions, int defaultBlockLuck) {}

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
        static ThemeSpawn forTheme(LuckyTheme theme) {
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
            };
        }
    }
}
