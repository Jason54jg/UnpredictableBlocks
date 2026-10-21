package com.jason54.unpredictableblocks.quest;

import com.jason54.unpredictableblocks.luck.LuckTier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

public final class JdrDiceRoll {
    private JdrDiceRoll() {}

    public static LuckTier rollD20(ServerPlayer player, RandomSource random) {
        int roll = random.nextInt(20) + 1;
        player.sendSystemMessage(Component.literal("🎲 D20 → " + roll));
        if (roll == 20) {
            player.sendSystemMessage(Component.literal("§aRéussite critique !"));
            return LuckTier.HIGH;
        }
        if (roll == 1) {
            player.sendSystemMessage(Component.literal("§cÉchec critique !"));
            return LuckTier.LOW;
        }
        return LuckTier.NEUTRAL;
    }
}
