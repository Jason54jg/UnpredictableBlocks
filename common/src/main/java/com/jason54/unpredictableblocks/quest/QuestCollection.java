package com.jason54.unpredictableblocks.quest;

import com.jason54.unpredictableblocks.registry.ModBlocks;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Advancement « collectionneur » : posséder les 32 Lucky Blocks (items). */
public final class QuestCollection {
    private QuestCollection() {}

    public static void tryAwardCollector(ServerPlayer player) {
        for (LuckyTheme theme : LuckyTheme.values()) {
            if (!hasAtLeastOne(player, ModBlocks.get(theme).asItem())) {
                return;
            }
        }
        QuestRewards.complete(player, "collector", "all_blocks");
    }

    private static boolean hasAtLeastOne(ServerPlayer player, Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item) && !stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
