package com.jason54.unpredictableblocks.world;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.config.PlatformConfigPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/** Profil choisi *avant* la création du monde (écran création / commande {@code world next}). */
public final class NextWorldProfileStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE = "next_world_profile.json";

    private NextWorldProfileStorage() {}

    public record Pending(WorldPlayMode mode, int oneBlockPlayers, WorldModeSettings settings) {
        public Pending {
            oneBlockPlayers = Math.clamp(oneBlockPlayers, 1, 4);
            if (settings == null) {
                settings = WorldModeSettings.defaults(mode);
            }
        }

        public Pending(WorldPlayMode mode, int oneBlockPlayers) {
            this(mode, oneBlockPlayers, WorldModeSettings.defaults(mode));
        }
    }

    public static void save(Pending pending) {
        Path path = PlatformConfigPaths.configFile(FILE);
        try {
            Files.createDirectories(path.getParent());
            WorldModeSettings s = pending.settings();
            Files.writeString(path, GSON.toJson(new FileDto(
                    pending.mode().id(),
                    pending.oneBlockPlayers(),
                    s.pathScale().name(),
                    s.luckyDensity().name(),
                    s.parcoursLayout().name(),
                    s.parcoursRules().name(),
                    s.arenaDecor().name(),
                    s.dailySeed(),
                    s.oneBlockEvolution()
            )));
        } catch (IOException e) {
            UnpredictableBlocks.LOGGER.error("Écriture next_world_profile.json impossible", e);
        }
    }

    /** Lit et supprime le fichier (usage unique à la création du monde). */
    public static Optional<Pending> consume() {
        Optional<Pending> pending = peek();
        clear();
        return pending;
    }

    /** Supprime le pending après application réussie. */
    public static void clear() {
        try {
            Files.deleteIfExists(PlatformConfigPaths.configFile(FILE));
        } catch (IOException e) {
            UnpredictableBlocks.LOGGER.warn("Suppression next_world_profile.json impossible", e);
        }
    }

    public static Optional<Pending> peek() {
        Path path = PlatformConfigPaths.configFile(FILE);
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        try {
            FileDto dto = GSON.fromJson(Files.readString(path), FileDto.class);
            return fromDto(dto);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static Optional<Pending> fromDto(FileDto dto) {
        if (dto == null || dto.mode == null) {
            return Optional.empty();
        }
        return WorldPlayMode.parse(dto.mode).map(mode -> {
            int players = dto.oneBlockPlayers == 0 ? 1 : dto.oneBlockPlayers;
            WorldModeSettings settings = readSettings(dto, mode);
            return new Pending(mode, players, settings);
        });
    }

    private static WorldModeSettings readSettings(FileDto dto, WorldPlayMode mode) {
        if (dto.pathScale == null) {
            return WorldModeSettings.defaults(mode);
        }
        return new WorldModeSettings(
                WorldModeSettings.parsePathScale(dto.pathScale).orElse(WorldModeSettings.PathScale.STANDARD),
                WorldModeSettings.parseDensity(dto.luckyDensity).orElse(WorldModeSettings.LuckyDensity.NORMAL),
                WorldModeSettings.parseLayout(dto.parcoursLayout).orElse(WorldModeSettings.ParcoursLayout.LANES),
                WorldModeSettings.parseRules(dto.parcoursRules).orElse(WorldModeSettings.ParcoursRules.STANDARD),
                WorldModeSettings.parseDecor(dto.arenaDecor).orElse(WorldModeSettings.ArenaDecor.VOID),
                dto.dailySeed,
                dto.oneBlockEvolution
        );
    }

    private record FileDto(
            String mode,
            int oneBlockPlayers,
            String pathScale,
            String luckyDensity,
            String parcoursLayout,
            String parcoursRules,
            String arenaDecor,
            boolean dailySeed,
            boolean oneBlockEvolution
    ) {
        FileDto(String mode, int oneBlockPlayers) {
            this(mode, oneBlockPlayers, null, null, null, null, null, false, false);
        }
    }
}
