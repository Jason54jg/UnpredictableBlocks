package com.jason54.unpredictableblocks.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

/** La Baguette — épée/bâton à knockback extrême. */
public class BaguetteMagiqueItem extends SwordItem {
    private final java.util.function.Consumer<ItemStack> stackSetup;

    public BaguetteMagiqueItem(java.util.function.Consumer<ItemStack> stackSetup) {
        super(Tiers.WOOD, new Properties().stacksTo(1));
        this.stackSetup = stackSetup;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stackSetup.accept(stack);
        return stack;
    }
}
