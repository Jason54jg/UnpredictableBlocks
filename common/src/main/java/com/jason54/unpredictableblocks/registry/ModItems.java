package com.jason54.unpredictableblocks.registry;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.item.LoreItem;
import com.jason54.unpredictableblocks.item.LuckyBlockItem;
import com.jason54.unpredictableblocks.item.UsableLuckItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public final class ModItems {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(UnpredictableBlocks.MOD_ID, Registries.ITEM);

    // Modificateurs de chance craft (+10 / -10)
    public static final RegistrySupplier<Item> LUCK_EMERALD_DUST = ITEMS.register("luck_emerald_dust",
            () -> new UsableLuckItem(new Item.Properties(), +10));
    public static final RegistrySupplier<Item> LUCK_BLAZE_POWDER_CURSE = ITEMS.register("luck_blaze_curse",
            () -> new UsableLuckItem(new Item.Properties(), -10));

    // Objets thématiques (échantillon — le reste suit le même pattern)
    public static final RegistrySupplier<Item> BAGUETTE_MAGIQUE = registerLore("baguette_magique");
    public static final RegistrySupplier<Item> CLE_USB_HACKER = registerLore("cle_usb_hacker");
    public static final RegistrySupplier<Item> MARTEAU_BONK = registerLore("marteau_bonk");
    public static final RegistrySupplier<Item> CASQUE_5G = registerLore("casque_5g");
    public static final RegistrySupplier<Item> SHAKER_PROT = registerLore("shaker_prot");
    public static final RegistrySupplier<Item> CLE_MOLETTE_LEGENDAIRE = registerLore("cle_molette_legendaire");
    public static final RegistrySupplier<Item> JETON_VIP = registerLore("jeton_or_vip");

    private ModItems() {}

    private static RegistrySupplier<Item> registerLore(String path) {
        return ITEMS.register(path, () -> new LoreItem(new Item.Properties()));
    }

    public static void register() {
        for (var theme : com.jason54.unpredictableblocks.theme.LuckyTheme.values()) {
            ITEMS.register("lucky_" + theme.id(), () ->
                    new LuckyBlockItem(ModBlocks.get(theme), new Item.Properties()));
        }
        ITEMS.register();
    }
}
