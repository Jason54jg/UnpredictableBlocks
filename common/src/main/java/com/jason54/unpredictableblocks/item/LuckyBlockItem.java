package com.jason54.unpredictableblocks.item;

import com.jason54.unpredictableblocks.block.entity.LuckyBlockEntity;
import com.jason54.unpredictableblocks.luck.LuckStat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LuckyBlockItem extends BlockItem {
    private static final String TAG_LUCK = "Luck";

    public LuckyBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static int readLuckFromStack(ItemStack stack) {
        var custom = stack.get(DataComponents.CUSTOM_DATA);
        if (custom == null) {
            return 0;
        }
        return LuckStat.clamp(custom.copyTag().getIntOr(TAG_LUCK, 0));
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        boolean placed = super.placeBlock(context, state);
        if (!placed || context.getLevel().isClientSide()) {
            return placed;
        }
        applyLuckFromStack(context.getLevel(), context.getClickedPos(), context.getItemInHand());
        return true;
    }

    @Override
    public boolean updateCustomBlockEntityTag(
            BlockPos pos,
            Level level,
            Player player,
            ItemStack stack,
            BlockState state
    ) {
        boolean updated = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        if (!level.isClientSide()) {
            applyLuckFromStack(level, pos, stack);
        }
        return updated;
    }

    private static void applyLuckFromStack(Level level, BlockPos pos, ItemStack stack) {
        int luck = readLuckFromStack(stack);
        if (luck == 0) {
            return;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LuckyBlockEntity luckyBlockEntity) {
            luckyBlockEntity.setLuck(luck);
        }
    }
}
