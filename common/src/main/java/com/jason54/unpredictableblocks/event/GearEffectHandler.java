package com.jason54.unpredictableblocks.event;

import com.jason54.unpredictableblocks.registry.ModItems;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * Effets passifs des objets équipables (Casque 5G, etc.).
 */
public final class GearEffectHandler {
    private GearEffectHandler() {}

    public static void register() {
        TickEvent.PLAYER.register(player -> {
            if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
                return;
            }
            var head = serverPlayer.getItemBySlot(EquipmentSlot.HEAD);
            if (head.is(ModItems.CASQUE_5G.get())) {
                purgeHarmfulEffects(serverPlayer);
            }
        });
    }

    private static void purgeHarmfulEffects(ServerPlayer player) {
        for (MobEffectInstance instance : player.getActiveEffects()) {
            if (instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                player.removeEffect(instance.getEffect());
            }
        }
    }
}
