package com.jason54.unpredictableblocks.registry;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.BuiltInRegistries;
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

    public static final RegistrySupplier<CreativeModeTab> MAIN = TABS.register("main", () ->
            CreativeTabRegistry.create(builder -> {
                builder.title(Component.translatable("itemGroup.unpredictableblocks.main"));
                builder.icon(() -> new ItemStack(ModBlocks.get(LuckyTheme.INVASION_MEMES)));
                builder.displayItems((params, output) -> {
                    for (var entry : BuiltInRegistries.ITEM.entrySet()) {
                        if (UnpredictableBlocks.MOD_ID.equals(entry.getKey().identifier().getNamespace())) {
                            output.accept(entry.getValue());
                        }
                    }
                });
            }));

    private ModCreativeTabs() {}

    public static void register() {
        TABS.register();
    }
}
