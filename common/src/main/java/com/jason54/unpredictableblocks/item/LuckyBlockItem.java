package com.jason54.unpredictableblocks.item;

import com.jason54.unpredictableblocks.block.LuckyBlock;
import com.jason54.unpredictableblocks.block.entity.LuckyBlockEntity;
import com.jason54.unpredictableblocks.compat.McAccess;
import com.jason54.unpredictableblocks.luck.LuckStat;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
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

    @Override
    public Component getName(ItemStack stack) {
        return getBlock().getName();
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

    public boolean updateCustomBlockEntityTag(
            BlockPos pos,
            Level level,
            Player player,
            ItemStack stack,
            BlockState state
    ) {
        boolean updated = McAccess.updateCustomBlockEntityTag(this, pos, level, player, stack, state);
        if (!level.isClientSide()) {
            applyLuckFromStack(level, pos, stack);
        }
        return updated;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        ModItemTooltips.appendDescription(this, tooltip);
        if (getBlock() instanceof LuckyBlock lucky) {
            var theme = lucky.theme();
            tooltip.accept(Component.translatable(
                    "tooltip.unpredictableblocks.rarity." + theme.rarity().name().toLowerCase()
            ).withStyle(ChatFormatting.DARK_PURPLE));
            ModItemTooltips.appendIfPresent(
                    tooltip,
                    "tooltip.unpredictableblocks.lucky_break",
                    ChatFormatting.DARK_AQUA);
            ModItemTooltips.appendLine(
                    tooltip,
                    "tooltip.unpredictableblocks.lucky_quest",
                    ChatFormatting.GOLD,
                    Component.translatable("advancements.unpredictableblocks." + theme.id() + ".description"));
            ModItemTooltips.appendIfPresent(
                    tooltip,
                    "tooltip.unpredictableblocks.lucky_luck_hint",
                    ChatFormatting.DARK_GRAY);
        }
        int luck = readLuckFromStack(stack);
        if (luck != 0) {
            tooltip.accept(Component.translatable("tooltip.unpredictableblocks.stored_luck", luck)
                    .withStyle(luck > 0 ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
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