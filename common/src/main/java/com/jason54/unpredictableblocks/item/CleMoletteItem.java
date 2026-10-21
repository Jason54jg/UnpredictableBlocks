package com.jason54.unpredictableblocks.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;

/** Clé à molette — pioche légendaire du thème Plomberie. */
public class CleMoletteItem extends PickaxeItem {
    private final java.util.function.Consumer<ItemStack> stackSetup;

    public CleMoletteItem(java.util.function.Consumer<ItemStack> stackSetup) {
        super(Tiers.NETHERITE, new Properties().stacksTo(1));
        this.stackSetup = stackSetup;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stackSetup.accept(stack);
        return stack;
    }
}
