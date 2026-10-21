package com.jason54.unpredictableblocks.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

/** Marteau BONK — compté pour la quête Mèmes. */
public class BonkHammerItem extends LoreItem {
    public BonkHammerItem(Properties properties, java.util.function.Consumer<ItemStack> setup) {
        super(properties, setup);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, net.minecraft.world.entity.LivingEntity entity) {
        if (entity.level() instanceof ServerLevel level) {
            level.playSound(null, entity.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.7f, 1.8f);
        }
        return super.onEntitySwing(stack, entity);
    }
}
