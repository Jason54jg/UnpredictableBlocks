package com.jason54.unpredictableblocks.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Arrays;
import java.util.List;

public final class ThemedGearHelper {
    private ThemedGearHelper() {}

    public static void name(ItemStack stack, String name) {
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
    }

    public static void lore(ItemStack stack, String... lines) {
        List<Component> components = Arrays.stream(lines).map(Component::literal).toList();
        stack.set(DataComponents.LORE, new ItemLore(components));
    }

    public static void enchant(ItemStack stack, Holder<Enchantment> enchantment, int level) {
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(
                stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
        );
        mutable.set(enchantment, level);
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
    }

    public static Holder<Enchantment> knockback() {
        return BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(Enchantments.KNOCKBACK);
    }

    public static Holder<Enchantment> efficiency() {
        return BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(Enchantments.EFFICIENCY);
    }

    public static Holder<Enchantment> unbreaking() {
        return BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(Enchantments.UNBREAKING);
    }

    public static Holder<Enchantment> protection() {
        return BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(Enchantments.PROTECTION);
    }
}
