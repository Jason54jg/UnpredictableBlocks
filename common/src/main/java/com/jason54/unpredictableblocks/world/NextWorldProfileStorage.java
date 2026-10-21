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

    public record Pending(WorldPlayMode mode, int oneBlockPlayers) {
        public Pending {
            oneBlockPlayers = Math.clamp(oneBlockPlayers, 1, 4);
        }
    }

    public static void save(Pending pending) {
        Path path = PlatformConfigPaths.configFile(FILE);
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(new FileDto(pending.mode().id(), pending.oneBlockPlayers())));
        } catch (IOException e) {
            UnpredictableBlocks.LOGGER.error("Écriture next_world_profile.json impossible", e);
        }
    }

    /** Lit et supprime le fichier (usage unique à la création du monde). */
    public static Optional<Pending> consume() {
        Path path = PlatformConfigPaths.configFile(FILE);
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        try {
            String json = Files.readString(path);
            FileDto dto = GSON.fromJson(json, FileDto.class);
            Files.deleteIfExists(path);
            if (dto == null || dto.mode == null) {
                return Optional.empty();
            }
            return WorldPlayMode.parse(dto.mode).map(m -> new Pending(m, dto.oneBlockPlayers == 0 ? 1 : dto.oneBlockPlayers));
        } catch (IOException e) {
            UnpredictableBlocks.LOGGER.warn("Lecture next_world_profile.json impossible", e);
            return Optional.empty();
        }
    }

    public static Optional<Pending> peek() {
        Path path = PlatformConfigPaths.configFile(FILE);
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        try {
            FileDto dto = GSON.fromJson(Files.readString(path), FileDto.class);
            if (dto == null || dto.mode == null) {
                return Optional.empty();
            }
            return WorldPlayMode.parse(dto.mode).map(m -> new Pending(m, dto.oneBlockPlayers == 0 ? 1 : dto.oneBlockPlayers));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private record FileDto(String mode, int oneBlockPlayers) {}
}
