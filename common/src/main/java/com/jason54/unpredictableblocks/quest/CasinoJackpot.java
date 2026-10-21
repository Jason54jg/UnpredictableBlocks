package com.jason54.unpredictableblocks.quest;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Machine à sous 777 lors de la casse d'un Lucky Block Casino.
 */
public final class CasinoJackpot {
    private CasinoJackpot() {}

    public static void spin(ServerLevel level, ServerPlayer player, RandomSource random) {
        int d1 = random.nextInt(10);
        int d2 = random.nextInt(10);
        int d3 = random.nextInt(10);
        player.sendSystemMessage(Component.literal("🎰 " + d1 + " | " + d2 + " | " + d3));

        if (d1 == 7 && d2 == 7 && d3 == 7) {
            player.sendSystemMessage(Component.literal("§6§lJACKPOT 777 !"));
            QuestRewards.complete(player, "quest/casino", "jackpot");
            player.getInventory().add(new net.minecraft.world.item.ItemStack(Items.EMERALD, 64));
            player.getInventory().add(new net.minecraft.world.item.ItemStack(Items.DIAMOND, 32));
            level.explode(null, player.getX(), player.getY(), player.getZ(), 0.0f, Level.ExplosionInteraction.NONE);
        }
    }
}
