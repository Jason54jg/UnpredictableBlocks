package com.jason54.unpredictableblocks.world;

import java.util.Locale;
import java.util.Optional;

/** Profils prêts à l'emploi (vote / menu / commandes). */
public final class WorldModePresets {
    private WorldModePresets() {}

    public static Optional<NextWorldProfileStorage.Pending> pending(String id, int players) {
        if (id == null) {
            return Optional.empty();
        }
        String key = id.trim().toLowerCase(Locale.ROOT);
        return switch (key) {
            case "parcours_race", "race" -> Optional.of(new NextWorldProfileStorage.Pending(
                    WorldPlayMode.PARCOURS, players, parcoursRace(WorldModeSettings.PathScale.STANDARD)));
            case "parcours_race_marathon", "race_marathon" -> Optional.of(new NextWorldProfileStorage.Pending(
                    WorldPlayMode.PARCOURS, players, parcoursRace(WorldModeSettings.PathScale.MARATHON)));
            case "parcours_coop", "coop" -> Optional.of(new NextWorldProfileStorage.Pending(
                    WorldPlayMode.PARCOURS, players, parcoursCoop(WorldModeSettings.PathScale.STANDARD)));
            case "parcours_battle", "battle" -> Optional.of(new NextWorldProfileStorage.Pending(
                    WorldPlayMode.PARCOURS, players, parcoursBattle(WorldModeSettings.PathScale.STANDARD)));
            case "parcours_roulette", "roulette" -> Optional.of(new NextWorldProfileStorage.Pending(
                    WorldPlayMode.PARCOURS, players, parcoursRoulette(WorldModeSettings.PathScale.STANDARD)));
            case "parcours_hardcore", "hardcore_parcours" -> Optional.of(new NextWorldProfileStorage.Pending(
                    WorldPlayMode.PARCOURS, players, parcoursHardcore(WorldModeSettings.PathScale.STANDARD)));
            case "parcours_daily", "daily" -> Optional.of(new NextWorldProfileStorage.Pending(
                    WorldPlayMode.PARCOURS, players, parcoursDaily(WorldModeSettings.PathScale.STANDARD)));
            case "parcours_island", "island" -> Optional.of(new NextWorldProfileStorage.Pending(
                    WorldPlayMode.PARCOURS, players, parcoursIsland(WorldModeSettings.PathScale.STANDARD)));
            case "oneblock_evolution", "evolution" -> Optional.of(new NextWorldProfileStorage.Pending(
                    WorldPlayMode.ONEBLOCK, players, oneBlockEvolution()));
            default -> Optional.empty();
        };
    }

    public static WorldModeSettings parcoursRace(WorldModeSettings.PathScale scale) {
        return new WorldModeSettings(scale, WorldModeSettings.LuckyDensity.NORMAL, WorldModeSettings.ParcoursLayout.LANES,
                WorldModeSettings.ParcoursRules.RACE, WorldModeSettings.ArenaDecor.VOID, false, false);
    }

    public static WorldModeSettings parcoursCoop(WorldModeSettings.PathScale scale) {
        return new WorldModeSettings(scale, WorldModeSettings.LuckyDensity.CARNIVAL, WorldModeSettings.ParcoursLayout.COOP,
                WorldModeSettings.ParcoursRules.STANDARD, WorldModeSettings.ArenaDecor.VOID, false, false);
    }

    public static WorldModeSettings parcoursBattle(WorldModeSettings.PathScale scale) {
        return new WorldModeSettings(scale, WorldModeSettings.LuckyDensity.NORMAL, WorldModeSettings.ParcoursLayout.BATTLE,
                WorldModeSettings.ParcoursRules.STANDARD, WorldModeSettings.ArenaDecor.VOID, false, false);
    }

    public static WorldModeSettings parcoursRoulette(WorldModeSettings.PathScale scale) {
        return new WorldModeSettings(scale, WorldModeSettings.LuckyDensity.NORMAL, WorldModeSettings.ParcoursLayout.LANES,
                WorldModeSettings.ParcoursRules.ROULETTE, WorldModeSettings.ArenaDecor.VOID, false, false);
    }

    public static WorldModeSettings parcoursHardcore(WorldModeSettings.PathScale scale) {
        return new WorldModeSettings(scale, WorldModeSettings.LuckyDensity.SPARSE, WorldModeSettings.ParcoursLayout.LANES,
                WorldModeSettings.ParcoursRules.HARDCORE, WorldModeSettings.ArenaDecor.VOID, false, false);
    }

    public static WorldModeSettings parcoursDaily(WorldModeSettings.PathScale scale) {
        return new WorldModeSettings(scale, WorldModeSettings.LuckyDensity.NORMAL, WorldModeSettings.ParcoursLayout.LANES,
                WorldModeSettings.ParcoursRules.STANDARD, WorldModeSettings.ArenaDecor.VOID, true, false);
    }

    public static WorldModeSettings parcoursIsland(WorldModeSettings.PathScale scale) {
        return new WorldModeSettings(scale, WorldModeSettings.LuckyDensity.NORMAL, WorldModeSettings.ParcoursLayout.LANES,
                WorldModeSettings.ParcoursRules.STANDARD, WorldModeSettings.ArenaDecor.ISLAND, false, false);
    }

    public static WorldModeSettings oneBlockEvolution() {
        return new WorldModeSettings(WorldModeSettings.PathScale.STANDARD, WorldModeSettings.LuckyDensity.NORMAL,
                WorldModeSettings.ParcoursLayout.LANES, WorldModeSettings.ParcoursRules.STANDARD,
                WorldModeSettings.ArenaDecor.VOID, false, true);
    }
}
