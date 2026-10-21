package com.jason54.unpredictableblocks.lucky;

import com.jason54.unpredictableblocks.config.ModConfig;
import com.jason54.unpredictableblocks.luck.LuckTier;
import com.jason54.unpredictableblocks.registry.ModItems;
import com.jason54.unpredictableblocks.util.ModMessages;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** LOW « serré » (luck proche du seuil −50) : réconfort + 1 poussière. */
public final class NearMissHandler {
    private NearMissHandler() {}

    public static void tryAward(ServerPlayer player, LuckTier tier, int effectiveLuck) {
        if (tier != LuckTier.LOW) {
            return;
        }
        var gameplay = ModConfig.gameplay();
        int min = gameplay.nearMissLuckMinOrDefault();
        int max = gameplay.nearMissLuckMaxOrDefault();
        if (effectiveLuck < min || effectiveLuck > max) {
            return;
        }
        player.sendSystemMessage(ModMessages.msg("near_miss"));
        player.getInventory().add(new ItemStack(ModItems.LUCK_EMERALD_DUST.get()));
    }
}
