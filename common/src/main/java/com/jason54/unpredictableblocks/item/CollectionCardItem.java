package com.jason54.unpredictableblocks.item;

import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Carte à collectionner ; tag NBT {@code Theme} = id thème. */
public class CollectionCardItem extends Item {
    private static final String TAG_THEME = "Theme";

    public CollectionCardItem(Properties properties) {
        super(properties);
    }

    public static ItemStack forTheme(LuckyTheme theme) {
        ItemStack stack = new ItemStack(com.jason54.unpredictableblocks.registry.ModItems.CARTE_COLLECTION.get());
        applyTheme(stack, theme);
        return stack;
    }

    public static void applyTheme(ItemStack stack, LuckyTheme theme) {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_THEME, theme.id());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Carte — " + theme.id()));
    }

    public static LuckyTheme readTheme(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        String id = data.copyTag().getStringOr(TAG_THEME, "");
        for (LuckyTheme theme : LuckyTheme.values()) {
            if (theme.id().equals(id)) {
                return theme;
            }
        }
        return null;
    }
}
