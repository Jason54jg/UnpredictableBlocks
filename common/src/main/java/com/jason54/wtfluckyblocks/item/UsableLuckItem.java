package com.jason54.wtfluckyblocks.item;

import com.jason54.wtfluckyblocks.luck.PlayerLuckStorage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class UsableLuckItem extends Item {
    private final int luckDelta;

    public UsableLuckItem(Properties properties, int luckDelta) {
        super(properties);
        this.luckDelta = luckDelta;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            PlayerLuckStorage.addModifier(serverPlayer, luckDelta);
            if (!player.getAbilities().instabuild) {
                player.getItemInHand(hand).shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }
}
