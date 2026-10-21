package com.jason54.unpredictableblocks.item;

import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;

public class Casque5GItem extends Item {
    private final java.util.function.Consumer<ItemStack> stackSetup;

    public Casque5GItem(java.util.function.Consumer<ItemStack> stackSetup) {
        super(new Properties().stacksTo(1).humanoidArmor(ArmorMaterials.GOLD, ArmorType.HELMET));
        this.stackSetup = stackSetup;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stackSetup.accept(stack);
        return stack;
    }
}
