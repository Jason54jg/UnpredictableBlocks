package com.jason54.unpredictableblocks.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** Item thematique avec setup de stack (nom/enchants) + description traduite. */
public class LoreItem extends Item {
    private final Consumer<ItemStack> stackSetup;

    public LoreItem(Properties properties, Consumer<ItemStack> stackSetup) {
        super(properties.stacksTo(1));
        this.stackSetup = stackSetup;
    }

    public LoreItem(Properties properties) {
        this(properties, stack -> {});
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stackSetup.accept(stack);
        return stack;
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
    }
}