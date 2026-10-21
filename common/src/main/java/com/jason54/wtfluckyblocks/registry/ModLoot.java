package com.jason54.wtfluckyblocks.registry;

import com.jason54.wtfluckyblocks.WtfLuckyBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public final class ModLoot {
    public static final ResourceLocation LUCKY_CONTEXT =
            ResourceLocation.fromNamespaceAndPath(WtfLuckyBlocks.MOD_ID, "lucky_block");

    private ModLoot() {}

    public static void register() {
        // Enregistrement côté plateforme si nécessaire ; le param set est déclaré en data.
    }

}
