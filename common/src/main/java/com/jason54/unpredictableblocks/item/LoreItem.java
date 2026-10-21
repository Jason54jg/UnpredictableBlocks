package com.jason54.unpredictableblocks.item;

import net.minecraft.world.item.Item;

/** Item avec lore/enchantements définis en data (item_modifiers / lang). */
public class LoreItem extends Item {
    public LoreItem(Properties properties) {
        super(properties.stacksTo(1));
    }
}
