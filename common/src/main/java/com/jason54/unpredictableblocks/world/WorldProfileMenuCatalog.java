package com.jason54.unpredictableblocks.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Entrées menu titre / commandes (modes + presets). */
public final class WorldProfileMenuCatalog {
    private WorldProfileMenuCatalog() {}

    public record Entry(String id, String label, NextWorldProfileStorage.Pending pending) {}

    public static List<Entry> coreModes() {
        List<Entry> list = new ArrayList<>();
        list.add(entry("normal", "Normal", WorldPlayMode.NORMAL, 1, null));
        list.add(entry("easy", "Easy", WorldPlayMode.EASY, 1, null));
        list.add(entry("hard", "Hard", WorldPlayMode.HARD, 1, null));
        list.add(entry("hardcore", "Hardcore", WorldPlayMode.HARDCORE, 1, null));
        for (int p = 1; p <= 4; p++) {
            list.add(entry("oneblock_" + p, "OneBlock " + p + "p", WorldPlayMode.ONEBLOCK, p, null));
        }
        for (int p = 1; p <= 4; p++) {
            list.add(entry("parcours_" + p, "Parcours " + p + "p", WorldPlayMode.PARCOURS, p, null));
        }
        return list;
    }

    public static List<Entry> presets() {
        List<Entry> list = new ArrayList<>();
        addPreset(list, "race", "Race");
        addPreset(list, "race_marathon", "Race Marathon");
        addPreset(list, "coop", "Coop");
        addPreset(list, "battle", "Battle");
        addPreset(list, "roulette", "Roulette");
        addPreset(list, "hardcore_parcours", "Hardcore Parcours");
        addPreset(list, "daily", "Daily seed");
        addPreset(list, "island", "Floating island");
        addPreset(list, "oneblock_evolution", "OneBlock Evolution");
        return list;
    }

    private static void addPreset(List<Entry> list, String presetId, String labelPrefix) {
        for (int players = 1; players <= 4; players++) {
            final int slot = players;
            WorldModePresets.pending(presetId, slot).ifPresent(pending ->
                    list.add(new Entry(
                            presetId + "_" + slot,
                            labelPrefix + " — " + slot + " player" + (slot > 1 ? "s" : ""),
                            pending
                    ))
            );
        }
    }

    private static Entry entry(String id, String label, WorldPlayMode mode, int players, WorldModeSettings settings) {
        NextWorldProfileStorage.Pending pending = settings == null
                ? new NextWorldProfileStorage.Pending(mode, players)
                : new NextWorldProfileStorage.Pending(mode, players, settings);
        return new Entry(id, label, pending);
    }

    public static String presetCommand(String presetId, int players) {
        return String.format(Locale.ROOT, "/unpredictableblocks world preset %s %d", presetId, players);
    }
}
