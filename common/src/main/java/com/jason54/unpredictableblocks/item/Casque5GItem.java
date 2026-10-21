package com.jason54.unpredictableblocks.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;

public class Casque5GItem extends ArmorItem {
    private final java.util.function.Consumer<ItemStack> stackSetup;

    public Casque5GItem(java.util.function.Consumer<ItemStack> stackSetup) {
        super(ArmorMaterials.GOLD, ArmorItem.Type.HELMET, new Properties().stacksTo(1));
        this.stackSetup = stackSetup;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stackSetup.accept(stack);
        return stack;
    }
}
