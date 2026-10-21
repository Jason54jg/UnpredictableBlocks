package com.jason54.unpredictableblocks.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;

/** La Baguette — épée/bâton à knockback extrême. */
public class BaguetteMagiqueItem extends Item {
    private final java.util.function.Consumer<ItemStack> stackSetup;

    public BaguetteMagiqueItem(Properties properties, java.util.function.Consumer<ItemStack> stackSetup) {
        super(properties.stacksTo(1).sword(ToolMaterial.WOOD, 3.0f, -2.4f));
        this.stackSetup = stackSetup;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stackSetup.accept(stack);
        return stack;
    }
}
