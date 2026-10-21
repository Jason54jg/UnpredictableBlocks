package com.jason54.unpredictableblocks.item;

import com.jason54.unpredictableblocks.quest.PlayerQuestData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class ItemCooldowns {
    public static final String SHAKER = "shaker_prot";
    public static final String JETON = "jeton_vip";
    public static final long SHAKER_TICKS = 20 * 60 * 3;
    public static final long JETON_TICKS = 20 * 60;

    private ItemCooldowns() {}

    public static boolean tryConsume(ServerPlayer player, String key, long cooldownTicks) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        if (PlayerQuestData.tryCooldown(player, key, cooldownTicks)) {
            return true;
        }
        long remaining = PlayerQuestData.cooldownRemaining(player, key);
        int seconds = (int) (remaining / 20);
        player.sendSystemMessage(Component.literal("§cRecharge : " + seconds + "s"));
        return false;
    }
}
