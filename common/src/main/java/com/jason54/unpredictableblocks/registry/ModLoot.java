package com.jason54.unpredictableblocks.registry;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public final class ModLoot {
    public static final ResourceLocation LUCKY_CONTEXT =
            ResourceLocation.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, "lucky_block");

    private ModLoot() {}

    public static void register() {
        // Enregistrement côté plateforme si nécessaire ; le param set est déclaré en data.
    }

}
