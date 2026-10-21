package com.jason54.unpredictableblocks.lucky;

import com.jason54.unpredictableblocks.luck.LuckTier;
import com.jason54.unpredictableblocks.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** LOW « serré » (luck proche du seuil −50) : réconfort + 1 poussière. */
public final class NearMissHandler {
    private NearMissHandler() {}

    public static void tryAward(ServerPlayer player, LuckTier tier, int effectiveLuck) {
        if (tier != LuckTier.LOW) {
            return;
        }
        if (effectiveLuck < -58 || effectiveLuck > -50) {
            return;
        }
        player.sendSystemMessage(Component.literal("§7Presque neutre… §8(near miss) §7→ §a+1 poussière"));
        player.getInventory().add(new ItemStack(ModItems.LUCK_EMERALD_DUST.get()));
    }
}
