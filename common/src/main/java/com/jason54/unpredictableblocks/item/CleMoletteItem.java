package com.jason54.unpredictableblocks.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;

/** Clé à molette — pioche légendaire du thème Plomberie. */
public class CleMoletteItem extends Item {
    private final java.util.function.Consumer<ItemStack> stackSetup;

    public CleMoletteItem(java.util.function.Consumer<ItemStack> stackSetup) {
        super(new Properties().stacksTo(1).pickaxe(ToolMaterial.NETHERITE, 1.0f, -2.8f));
        this.stackSetup = stackSetup;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stackSetup.accept(stack);
        return stack;
    }
}
