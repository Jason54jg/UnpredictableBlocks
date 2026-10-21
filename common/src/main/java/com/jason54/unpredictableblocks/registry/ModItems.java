package com.jason54.unpredictableblocks.registry;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.item.BonkHammerItem;
import com.jason54.unpredictableblocks.item.CleUsbHackerItem;
import com.jason54.unpredictableblocks.item.LoreItem;
import com.jason54.unpredictableblocks.item.LuckyBlockItem;
import com.jason54.unpredictableblocks.item.ThemedGearHelper;
import com.jason54.unpredictableblocks.item.UsableLuckItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public final class ModItems {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(UnpredictableBlocks.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Item> LUCK_EMERALD_DUST = ITEMS.register("luck_emerald_dust",
            () -> new UsableLuckItem(new Item.Properties(), +10));
    public static final RegistrySupplier<Item> LUCK_BLAZE_POWDER_CURSE = ITEMS.register("luck_blaze_curse",
            () -> new UsableLuckItem(new Item.Properties(), -10));

    public static final RegistrySupplier<Item> BAGUETTE_MAGIQUE = ITEMS.register("baguette_magique", () ->
            new LoreItem(new Item.Properties(), stack -> {
                ThemedGearHelper.name(stack, "La Baguette Magique");
                ThemedGearHelper.lore(stack, "Knockback absurde.", "Du pain. Beaucoup de pain.");
                ThemedGearHelper.enchant(stack, ThemedGearHelper.knockback(), 10);
            }));

    public static final RegistrySupplier<Item> CLE_USB_HACKER = ITEMS.register("cle_usb_hacker", () ->
            new CleUsbHackerItem(new Item.Properties(), stack -> {
                ThemedGearHelper.name(stack, "Clé USB du Hacker");
                ThemedGearHelper.lore(stack, "Clic droit : overclock 30s.");
            }));

    public static final RegistrySupplier<Item> MARTEAU_BONK = ITEMS.register("marteau_bonk", () ->
            new BonkHammerItem(new Item.Properties(), stack -> {
                ThemedGearHelper.name(stack, "Marteau BONK");
                ThemedGearHelper.lore(stack, "BONK!", "Quête : 50 coups.");
                ThemedGearHelper.enchant(stack, ThemedGearHelper.knockback(), 5);
            }));

    public static final RegistrySupplier<Item> CASQUE_5G = ITEMS.register("casque_5g", () ->
            new LoreItem(new Item.Properties(), stack -> {
                ThemedGearHelper.name(stack, "Casque 5G en Papier d'Aluminium");
                ThemedGearHelper.lore(stack, "Immunité aux ondes et aux effets.");
            }));

    public static final RegistrySupplier<Item> SHAKER_PROT = ITEMS.register("shaker_prot", () ->
            new LoreItem(new Item.Properties(), stack -> {
                ThemedGearHelper.name(stack, "Shaker de Prot' Pur Sérotonine");
                ThemedGearHelper.lore(stack, "Force, vitesse, faim.");
            }));

    public static final RegistrySupplier<Item> CLE_MOLETTE_LEGENDAIRE = ITEMS.register("cle_molette_legendaire", () ->
            new LoreItem(new Item.Properties(), stack -> {
                ThemedGearHelper.name(stack, "Clé à Molette Légendaire");
                ThemedGearHelper.lore(stack, "Efficiency IV — le royaume coule.");
            }));

    public static final RegistrySupplier<Item> JETON_VIP = ITEMS.register("jeton_or_vip", () ->
            new LoreItem(new Item.Properties(), stack -> {
                ThemedGearHelper.name(stack, "Jeton d'Or V.I.P.");
                ThemedGearHelper.lore(stack, "Relance un Lucky Block manqué.");
            }));

    private ModItems() {}

    public static void register() {
        for (var theme : com.jason54.unpredictableblocks.theme.LuckyTheme.values()) {
            ITEMS.register("lucky_" + theme.id(), () ->
                    new LuckyBlockItem(ModBlocks.get(theme), new Item.Properties()));
        }
        ITEMS.register();
    }
}
