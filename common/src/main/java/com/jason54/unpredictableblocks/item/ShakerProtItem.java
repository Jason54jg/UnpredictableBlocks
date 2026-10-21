package com.jason54.unpredictableblocks.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShakerProtItem extends LoreItem {
    public ShakerProtItem(Properties properties, java.util.function.Consumer<ItemStack> setup) {
        super(properties, setup);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }
        if (!ItemCooldowns.tryConsume(serverPlayer, ItemCooldowns.SHAKER, ItemCooldowns.SHAKER_TICKS)) {
            return InteractionResult.FAIL;
        }
        serverPlayer.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 20 * 90, 2));
        serverPlayer.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 90, 1));
        serverPlayer.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20 * 90, 1));
        if (!player.getAbilities().instabuild) {
            player.getItemInHand(hand).shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
