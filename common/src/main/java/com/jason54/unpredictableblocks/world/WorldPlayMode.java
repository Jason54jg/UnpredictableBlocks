package com.jason54.unpredictableblocks.world;

import java.util.Locale;
import java.util.Optional;

/** Mode de jeu UnpredictableBlocks pour un monde (choix à la création ou au 1er lancement). */
public enum WorldPlayMode {
    NORMAL,
    EASY,
    HARD,
    HARDCORE,
    ONEBLOCK;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Optional<WorldPlayMode> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String norm = raw.trim().toLowerCase(Locale.ROOT).replace('-', '_');
        for (WorldPlayMode mode : values()) {
            if (mode.id().equals(norm) || mode.name().equalsIgnoreCase(raw)) {
                return Optional.of(mode);
            }
        }
        if ("one_block".equals(norm) || "oneblock".equals(norm)) {
            return Optional.of(ONEBLOCK);
        }
        return Optional.empty();
    }
}
