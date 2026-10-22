package com.jason54.unpredictableblocks.quest;

import com.jason54.unpredictableblocks.lucky.PlayerLuckyBreakStats;
import com.jason54.unpredictableblocks.network.OpenQuestJournalPayload;
import com.jason54.unpredictableblocks.network.QuestThemeProgress;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import dev.architectury.networking.NetworkManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

/** Construit et envoie le snapshot de progression pour l'UI journal. */
public final class QuestJournal {
    private QuestJournal() {}

    public static void open(ServerPlayer player) {
        List<QuestThemeProgress> themes = new ArrayList<>(LuckyTheme.values().length);
        for (LuckyTheme theme : LuckyTheme.values()) {
            themes.add(new QuestThemeProgress(
                    theme.id(),
                    isQuestDone(player, theme),
                    QuestRewards.isDone(player, "theme_master/" + theme.id()),
                    CardCollection.hasCard(player, theme),
                    PlayerLuckyBreakStats.breaks(player, theme)
            ));
        }
        boolean casino = QuestRewards.isDone(player, "quest/casino");
        NetworkManager.sendToPlayer(player, new OpenQuestJournalPayload(themes, casino));

        QuestCollection.tryAwardCollector(player);
        CardCollection.tryAwardCardAlbum(player);
        QuestMilestones.checkAfterQuest(player);
        ThemeMasterMilestones.checkAfterMaster(player);
    }

    private static boolean isQuestDone(ServerPlayer player, LuckyTheme theme) {
        return QuestRewards.isDone(player, "quest/" + theme.id());
    }
}
