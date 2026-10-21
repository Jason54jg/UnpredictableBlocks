package com.jason54.unpredictableblocks.world;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;

/** Réglages avancés des modes (parcours, oneblock, etc.). */
public record WorldModeSettings(
        PathScale pathScale,
        LuckyDensity luckyDensity,
        ParcoursLayout parcoursLayout,
        ParcoursRules parcoursRules,
        ArenaDecor arenaDecor,
        boolean dailySeed,
        boolean oneBlockEvolution
) {
    public static WorldModeSettings defaults(WorldPlayMode mode) {
        return switch (mode) {
            case PARCOURS -> new WorldModeSettings(
                    PathScale.STANDARD,
                    LuckyDensity.NORMAL,
                    ParcoursLayout.LANES,
                    ParcoursRules.STANDARD,
                    ArenaDecor.VOID,
                    false,
                    false
            );
            case ONEBLOCK -> new WorldModeSettings(
                    PathScale.STANDARD,
                    LuckyDensity.NORMAL,
                    ParcoursLayout.LANES,
                    ParcoursRules.STANDARD,
                    ArenaDecor.VOID,
                    false,
                    true
            );
            default -> new WorldModeSettings(
                    PathScale.STANDARD,
                    LuckyDensity.NORMAL,
                    ParcoursLayout.LANES,
                    ParcoursRules.STANDARD,
                    ArenaDecor.VOID,
                    false,
                    false
            );
        };
    }

    public int pathLengthBlocks() {
        return pathScale.length;
    }

    public int luckyStepInterval() {
        return luckyDensity.interval;
    }

    public long layoutSeed(long worldSeed) {
        if (!dailySeed) {
            return worldSeed;
        }
        LocalDate day = LocalDate.now();
        return day.toEpochDay() * 31_415_926_535L ^ worldSeed;
    }

    public static Optional<PathScale> parsePathScale(String raw) {
        return PathScale.parse(raw);
    }

    public static Optional<LuckyDensity> parseDensity(String raw) {
        return LuckyDensity.parse(raw);
    }

    public static Optional<ParcoursLayout> parseLayout(String raw) {
        return ParcoursLayout.parse(raw);
    }

    public static Optional<ParcoursRules> parseRules(String raw) {
        return ParcoursRules.parse(raw);
    }

    public static Optional<ArenaDecor> parseDecor(String raw) {
        return ArenaDecor.parse(raw);
    }

    public enum PathScale {
        SPRINT(30),
        STANDARD(52),
        MARATHON(100);

        public final int length;

        PathScale(int length) {
            this.length = length;
        }

        static Optional<PathScale> parse(String raw) {
            if (raw == null || raw.isBlank()) {
                return Optional.empty();
            }
            String n = raw.trim().toLowerCase(Locale.ROOT);
            for (PathScale s : values()) {
                if (s.name().equalsIgnoreCase(n) || n.equals(String.valueOf(s.length))) {
                    return Optional.of(s);
                }
            }
            return Optional.empty();
        }
    }

    public enum LuckyDensity {
        SPARSE(8),
        NORMAL(5),
        CARNIVAL(3);

        public final int interval;

        LuckyDensity(int interval) {
            this.interval = interval;
        }

        static Optional<LuckyDensity> parse(String raw) {
            if (raw == null || raw.isBlank()) {
                return Optional.empty();
            }
            String n = raw.trim().toLowerCase(Locale.ROOT);
            for (LuckyDensity d : values()) {
                if (d.name().equalsIgnoreCase(n)) {
                    return Optional.of(d);
                }
            }
            return Optional.empty();
        }
    }

    public enum ParcoursLayout {
        LANES,
        COOP,
        BATTLE;

        static Optional<ParcoursLayout> parse(String raw) {
            if (raw == null || raw.isBlank()) {
                return Optional.empty();
            }
            String n = raw.trim().toLowerCase(Locale.ROOT);
            for (ParcoursLayout l : values()) {
                if (l.name().equalsIgnoreCase(n)) {
                    return Optional.of(l);
                }
            }
            return Optional.empty();
        }
    }

    public enum ParcoursRules {
        STANDARD,
        RACE,
        ROULETTE,
        HARDCORE;

        static Optional<ParcoursRules> parse(String raw) {
            if (raw == null || raw.isBlank()) {
                return Optional.empty();
            }
            String n = raw.trim().toLowerCase(Locale.ROOT);
            for (ParcoursRules r : values()) {
                if (r.name().equalsIgnoreCase(n)) {
                    return Optional.of(r);
                }
            }
            return Optional.empty();
        }
    }

    public enum ArenaDecor {
        VOID,
        ISLAND;

        static Optional<ArenaDecor> parse(String raw) {
            if (raw == null || raw.isBlank()) {
                return Optional.empty();
            }
            String n = raw.trim().toLowerCase(Locale.ROOT);
            for (ArenaDecor d : values()) {
                if (d.name().equalsIgnoreCase(n)) {
                    return Optional.of(d);
                }
            }
            return Optional.empty();
        }
    }
}
