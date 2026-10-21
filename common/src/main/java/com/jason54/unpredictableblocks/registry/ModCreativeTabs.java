package com.jason54.unpredictableblocks.registry;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTabs {
    public static final ResourceKey<CreativeModeTab> MAIN_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, "main"));

    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(UnpredictableBlocks.MOD_ID, Registries.CREATIVE_MODE_TAB);

    /** Contenu via {@code Item.Properties.arch$tab(MAIN_KEY)} dans {@link ModItems}. */
    public static final RegistrySupplier<CreativeModeTab> MAIN = TABS.register("main", () ->
            CreativeTabRegistry.create(
                    Component.translatable("itemGroup.unpredictableblocks.main"),
                    () -> new ItemStack(ModBlocks.get(LuckyTheme.INVASION_MEMES))));

    private ModCreativeTabs() {}

    public static void register() {
        TABS.register();
    }
}
