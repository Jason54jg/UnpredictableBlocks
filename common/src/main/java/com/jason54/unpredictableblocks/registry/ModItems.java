package com.jason54.unpredictableblocks.registry;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.item.BaguetteMagiqueItem;
import com.jason54.unpredictableblocks.item.BonkHammerItem;
import com.jason54.unpredictableblocks.item.Casque5GItem;
import com.jason54.unpredictableblocks.item.CleMoletteItem;
import com.jason54.unpredictableblocks.item.CleUsbHackerItem;
import com.jason54.unpredictableblocks.item.JetonVipItem;
import com.jason54.unpredictableblocks.item.ShakerProtItem;
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
            new BaguetteMagiqueItem(stack -> {
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
            new Casque5GItem(stack -> {
                ThemedGearHelper.name(stack, "Casque 5G en Papier d'Aluminium");
                ThemedGearHelper.lore(stack, "Immunité aux effets néfastes.");
                ThemedGearHelper.enchant(stack, ThemedGearHelper.protection(), 4);
            }));

    public static final RegistrySupplier<Item> SHAKER_PROT = ITEMS.register("shaker_prot", () ->
            new ShakerProtItem(new Item.Properties(), stack -> {
                ThemedGearHelper.name(stack, "Shaker de Prot' Pur Sérotonine");
                ThemedGearHelper.lore(stack, "Force III, Vitesse II, Faim II — 90s.");
            }));

    public static final RegistrySupplier<Item> CLE_MOLETTE_LEGENDAIRE = ITEMS.register("cle_molette_legendaire", () ->
            new CleMoletteItem(stack -> {
                ThemedGearHelper.name(stack, "Clé à Molette Légendaire");
                ThemedGearHelper.lore(stack, "Efficiency IV, Unbreaking III, Knockback II.");
                ThemedGearHelper.enchant(stack, ThemedGearHelper.efficiency(), 4);
                ThemedGearHelper.enchant(stack, ThemedGearHelper.unbreaking(), 3);
                ThemedGearHelper.enchant(stack, ThemedGearHelper.knockback(), 2);
            }));

    public static final RegistrySupplier<Item> JETON_VIP = ITEMS.register("jeton_or_vip", () ->
            new JetonVipItem(new Item.Properties(), stack -> {
                ThemedGearHelper.name(stack, "Jeton d'Or V.I.P.");
                ThemedGearHelper.lore(stack, "Relance le dernier malus en HIGH.");
            }));

    public static final RegistrySupplier<Item> ESPRESSO_ULTRA_DOSE = ITEMS.register("espresso_ultra_dose", ExtendedModItems::espressoUltraDose);
    public static final RegistrySupplier<Item> VALISE_PERDUE = ITEMS.register("valise_perdue", ExtendedModItems::valisePerdue);
    public static final RegistrySupplier<Item> TRANCHE_DEBAT = ITEMS.register("tranche_debat", ExtendedModItems::trancheDebat);
    public static final RegistrySupplier<Item> CHRONO_WORLD_RECORD = ITEMS.register("chrono_world_record", ExtendedModItems::chronoWorldRecord);
    public static final RegistrySupplier<Item> DISQUE_DUR_CAPITAINE = ITEMS.register("disque_dur_capitaine", ExtendedModItems::disqueDurCapitaine);
    public static final RegistrySupplier<Item> POP_IT_SUPREME = ITEMS.register("pop_it_supreme", ExtendedModItems::popItSupreme);
    public static final RegistrySupplier<Item> FIOLE_SANG_NOBLE = ITEMS.register("fiole_sang_noble", ExtendedModItems::fioleSangNoble);
    public static final RegistrySupplier<Item> ZAPPER_DIMENSIONS = ITEMS.register("zapper_dimensions", ExtendedModItems::zapperDimensions);

    private ModItems() {}

    public static void register() {
        for (var theme : com.jason54.unpredictableblocks.theme.LuckyTheme.values()) {
            ITEMS.register("lucky_" + theme.id(), () ->
                    new LuckyBlockItem(ModBlocks.get(theme), new Item.Properties()));
        }
        ITEMS.register();
    }
}
