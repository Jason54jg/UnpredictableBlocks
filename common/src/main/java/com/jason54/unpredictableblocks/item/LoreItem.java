package com.jason54.unpredictableblocks.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/** Item thématique avec nom/lore/enchants appliqués au stack par défaut. */
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
}
