package com.jason54.unpredictableblocks.quest;

import com.jason54.unpredictableblocks.registry.ModItems;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class QuestEventHandler {
    private QuestEventHandler() {}

    public static void register() {
        PlayerEvent.ATTACK_ENTITY.register((player, level, target, hand, hit) -> {
            onAttack(player, target);
            return EventResult.pass();
        });
    }

    private static void onAttack(Player player, Entity target) {
        if (player.level().isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (!held.is(ModItems.MARTEAU_BONK.get())) {
            return;
        }
        int bonks = PlayerQuestData.incrementBonk(serverPlayer);
        if (bonks >= 50) {
            QuestRewards.complete(serverPlayer, "quest/invasion_memes", "completed");
        }
    }
}
