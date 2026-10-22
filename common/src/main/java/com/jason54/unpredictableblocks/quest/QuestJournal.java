package com.jason54.unpredictableblocks.quest;

import com.jason54.unpredictableblocks.config.ModConfig;
import com.jason54.unpredictableblocks.lucky.PlayerLuckyBreakStats;
import com.jason54.unpredictableblocks.network.OpenQuestJournalPayload;
import com.jason54.unpredictableblocks.network.QuestThemeProgress;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import dev.architectury.networking.NetworkManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

/** Construit et envoie le snapshot de progression pour l'UI journal. */
public final class QuestJournal {
    private QuestJournal() {}

    public static void open(ServerPlayer player) {
        int masterBreaks = ModConfig.gameplay().themeMasterBreaksOrDefault();
        List<QuestThemeProgress> themes = new ArrayList<>(LuckyTheme.values().length);
        for (LuckyTheme theme : LuckyTheme.values()) {
            List<QuestThemeProgress.Step> steps = new ArrayList<>();
            for (Quests.Step step : Quests.steps(theme)) {
                steps.add(new QuestThemeProgress.Step(step.criterion(), Quests.progress(player, theme, step), step.target()));
            }
            themes.add(new QuestThemeProgress(
                    theme.id(),
                    theme.rarity().ordinal(),
                    Quests.isDone(player, theme),
                    QuestRewards.isDone(player, "theme_master/" + theme.id()),
                    CardCollection.hasCard(player, theme),
                    PlayerLuckyBreakStats.breaks(player, theme),
                    masterBreaks,
                    steps
            ));
        }
        NetworkManager.sendToPlayer(player, new OpenQuestJournalPayload(themes, suggest(themes)));

        QuestCollection.tryAwardCollector(player);
        CardCollection.tryAwardCardAlbum(player);
        QuestMilestones.checkAfterQuest(player);
        ThemeMasterMilestones.checkAfterMaster(player);
    }

    /**
     * Quête conseillée : d'abord celles des thèmes déjà cassés (le joueur a le contexte),
     * puis par rareté (communes avant légendaires), puis la plus avancée.
     */
    private static String suggest(List<QuestThemeProgress> themes) {
        return themes.stream()
                .filter(t -> !t.questDone())
                .min(Comparator.<QuestThemeProgress>comparingInt(t -> t.breaks() > 0 ? 0 : 1)
                        .thenComparingInt(QuestThemeProgress::rarity)
                        .thenComparing(t -> -t.questFraction()))
                .map(QuestThemeProgress::themeId)
                .orElse("");
    }
}
