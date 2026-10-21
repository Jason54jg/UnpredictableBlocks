package com.jason54.unpredictableblocks.quest;

import com.jason54.unpredictableblocks.config.ModConfig;
import com.jason54.unpredictableblocks.lucky.PlayerLuckyBreakStats;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import com.jason54.unpredictableblocks.util.ModMessages;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Affiche la progression des 32 quêtes thématiques. */
public final class QuestJournal {
    private QuestJournal() {}

    public static void open(ServerPlayer player) {
        int done = 0;
        for (LuckyTheme theme : LuckyTheme.values()) {
            if (isQuestDone(player, theme)) {
                done++;
            }
        }
        int cards = CardCollection.uniqueCount(player);
        int masters = ThemeMasterMilestones.countMasters(player);
        player.sendSystemMessage(ModMessages.msg("journal_header", done, cards, masters));
        for (LuckyTheme theme : LuckyTheme.values()) {
            String mark = isQuestDone(player, theme) ? "§a✔" : "§7○";
            Component title = Component.translatable("advancements.unpredictableblocks." + theme.id() + ".title");
            var spawn = ModConfig.themeSpawn(theme);
            String biomes = String.join(", ", spawn.preferredBiomes());
            String night = spawn.nightOnly() ? " §8(nuit)" : "";
            int br = PlayerLuckyBreakStats.breaks(player, theme);
            String master = QuestRewards.isDone(player, "theme_master/" + theme.id()) ? " §6★" : "";
            String card = CardCollection.hasCard(player, theme) ? " §b◆" : "";
            player.sendSystemMessage(Component.literal(
                    mark + card + master + " §8[" + br + " breaks] §7" + biomes + night + " — "
            ).append(title));
        }
        QuestCollection.tryAwardCollector(player);
        CardCollection.tryAwardCardAlbum(player);
        QuestMilestones.checkAfterQuest(player);
        ThemeMasterMilestones.checkAfterMaster(player);
    }

    private static boolean isQuestDone(ServerPlayer player, LuckyTheme theme) {
        return QuestRewards.isDone(player, "quest/" + theme.id());
    }
}
