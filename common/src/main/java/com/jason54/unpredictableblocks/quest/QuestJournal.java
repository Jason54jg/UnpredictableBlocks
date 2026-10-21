package com.jason54.unpredictableblocks.quest;

import com.jason54.unpredictableblocks.config.ModConfig;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
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
        player.sendSystemMessage(Component.literal("§6§lJournal des quêtes §8(" + done + "/32)"));
        for (LuckyTheme theme : LuckyTheme.values()) {
            String mark = isQuestDone(player, theme) ? "§a✔" : "§7○";
            String disabled = ModConfig.isThemeEnabled(theme) ? "" : " §8[off]";
            Component title = Component.translatable("advancements.unpredictableblocks." + theme.id() + ".title");
            player.sendSystemMessage(Component.literal(mark + " ").append(title).append(Component.literal(disabled)));
        }
        QuestCollection.tryAwardCollector(player);
    }

    private static boolean isQuestDone(ServerPlayer player, LuckyTheme theme) {
        return QuestRewards.isDone(player, "quest/" + theme.id());
    }
}
