package com.jason54.unpredictableblocks.registry;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(UnpredictableBlocks.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> MAIN = TABS.register("main", () ->
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.unpredictableblocks.main"))
                    .icon(() -> new ItemStack(ModBlocks.get(LuckyTheme.INVASION_MEMES)))
                    .displayItems((parameters, output) -> {
                        for (LuckyTheme theme : LuckyTheme.values()) {
                            output.accept(ModBlocks.get(theme));
                        }
                        output.accept(ModItems.BAGUETTE_MAGIQUE.get());
                        output.accept(ModItems.MARTEAU_BONK.get());
                        output.accept(ModItems.CASQUE_5G.get());
                        output.accept(ModItems.CLE_USB_HACKER.get());
                        output.accept(ModItems.SHAKER_PROT.get());
                        output.accept(ModItems.CLE_MOLETTE_LEGENDAIRE.get());
                        output.accept(ModItems.LUCKY_MYSTERE.get());
                        output.accept(ModItems.JOURNAL_QUETES.get());
                        output.accept(ModItems.JETON_VIP.get());
                        output.accept(ModItems.LUCK_EMERALD_DUST.get());
                        output.accept(ModItems.LUCK_BLAZE_POWDER_CURSE.get());
                        output.accept(ModItems.ESPRESSO_ULTRA_DOSE.get());
                        output.accept(ModItems.VALISE_PERDUE.get());
                        output.accept(ModItems.TRANCHE_DEBAT.get());
                        output.accept(ModItems.CHRONO_WORLD_RECORD.get());
                        output.accept(ModItems.DISQUE_DUR_CAPITAINE.get());
                        output.accept(ModItems.POP_IT_SUPREME.get());
                        output.accept(ModItems.FIOLE_SANG_NOBLE.get());
                        output.accept(ModItems.ZAPPER_DIMENSIONS.get());
                    })
                    .build());

    public static void register() {
        TABS.register();
    }
}
