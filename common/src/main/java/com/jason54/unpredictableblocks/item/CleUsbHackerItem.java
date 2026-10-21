package com.jason54.unpredictableblocks.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CleUsbHackerItem extends LoreItem {
    public CleUsbHackerItem(Properties properties, java.util.function.Consumer<ItemStack> setup) {
        super(properties, setup);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 20 * 30, 2));
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 30, 1));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }
}
