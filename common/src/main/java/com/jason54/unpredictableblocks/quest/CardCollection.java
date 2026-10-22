package com.jason54.unpredictableblocks.quest;

import com.jason54.unpredictableblocks.item.CollectionCardItem;
import com.jason54.unpredictableblocks.registry.ModItems;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Advancement « album de cartes » : une carte par thème (31). */
public final class CardCollection {
    private CardCollection() {}

    public static boolean hasCard(ServerPlayer player, LuckyTheme theme) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.is(ModItems.CARTE_COLLECTION.get())) {
                continue;
            }
            LuckyTheme onCard = CollectionCardItem.readTheme(stack);
            if (onCard == theme) {
                return true;
            }
        }
        return false;
    }

    public static int uniqueCount(ServerPlayer player) {
        int n = 0;
        for (LuckyTheme theme : LuckyTheme.values()) {
            if (hasCard(player, theme)) {
                n++;
            }
        }
        return n;
    }

    public static void tryAwardCardAlbum(ServerPlayer player) {
        for (LuckyTheme theme : LuckyTheme.values()) {
            if (!hasCard(player, theme)) {
                return;
            }
        }
        QuestRewards.complete(player, "card_collector", "all_cards");
    }
}
