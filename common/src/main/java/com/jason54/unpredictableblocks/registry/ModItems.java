package com.jason54.unpredictableblocks.registry;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.item.CollectionCardItem;
import com.jason54.unpredictableblocks.item.BaguetteMagiqueItem;
import com.jason54.unpredictableblocks.item.BonkHammerItem;
import com.jason54.unpredictableblocks.item.Casque5GItem;
import com.jason54.unpredictableblocks.item.CleMoletteItem;
import com.jason54.unpredictableblocks.item.CleUsbHackerItem;
import com.jason54.unpredictableblocks.item.ShakerProtItem;
import com.jason54.unpredictableblocks.item.LuckyBlockItem;
import com.jason54.unpredictableblocks.item.MysteryLuckyItem;
import com.jason54.unpredictableblocks.item.QuestJournalItem;
import com.jason54.unpredictableblocks.item.ThemedGearHelper;
import com.jason54.unpredictableblocks.item.UsableLuckItem;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class ModItems {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(UnpredictableBlocks.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Item> LUCK_EMERALD_DUST = ITEMS.register("luck_emerald_dust",
            () -> new UsableLuckItem(props("luck_emerald_dust"), +10));
    public static final RegistrySupplier<Item> LUCK_BLAZE_POWDER_CURSE = ITEMS.register("luck_blaze_curse",
            () -> new UsableLuckItem(props("luck_blaze_curse"), -10));

    public static final RegistrySupplier<Item> BAGUETTE_MAGIQUE = ITEMS.register("baguette_magique", () ->
            new BaguetteMagiqueItem(props("baguette_magique"), stack -> {
                ThemedGearHelper.name(stack, "La Baguette Magique");
                ThemedGearHelper.lore(stack, "Knockback absurde.", "Du pain. Beaucoup de pain.");
                ThemedGearHelper.enchant(stack, ThemedGearHelper.knockback(), 10);
            }));

    public static final RegistrySupplier<Item> CLE_USB_HACKER = ITEMS.register("cle_usb_hacker", () ->
            new CleUsbHackerItem(props("cle_usb_hacker"), stack -> {
                ThemedGearHelper.name(stack, "Clé USB du Hacker");
                ThemedGearHelper.lore(stack, "Clic droit : overclock 30s.");
            }));

    public static final RegistrySupplier<Item> MARTEAU_BONK = ITEMS.register("marteau_bonk", () ->
            new BonkHammerItem(props("marteau_bonk"), stack -> {
                ThemedGearHelper.name(stack, "Marteau BONK");
                ThemedGearHelper.lore(stack, "BONK!", "Quête : 50 coups.");
                ThemedGearHelper.enchant(stack, ThemedGearHelper.knockback(), 5);
            }));

    public static final RegistrySupplier<Item> CASQUE_5G = ITEMS.register("casque_5g", () ->
            new Casque5GItem(props("casque_5g"), stack -> {
                ThemedGearHelper.name(stack, "Casque 5G en Papier d'Aluminium");
                ThemedGearHelper.lore(stack, "Immunité aux effets néfastes.");
                ThemedGearHelper.enchant(stack, ThemedGearHelper.protection(), 4);
            }));

    public static final RegistrySupplier<Item> SHAKER_PROT = ITEMS.register("shaker_prot", () ->
            new ShakerProtItem(props("shaker_prot"), stack -> {
                ThemedGearHelper.name(stack, "Shaker de Prot' Pur Sérotonine");
                ThemedGearHelper.lore(stack, "Force III, Vitesse II, Faim II — 90s.");
            }));

    public static final RegistrySupplier<Item> CLE_MOLETTE_LEGENDAIRE = ITEMS.register("cle_molette_legendaire", () ->
            new CleMoletteItem(props("cle_molette_legendaire"), stack -> {
                ThemedGearHelper.name(stack, "Clé à Molette Légendaire");
                ThemedGearHelper.lore(stack, "Efficiency IV, Unbreaking III, Knockback II.");
                ThemedGearHelper.enchant(stack, ThemedGearHelper.efficiency(), 4);
                ThemedGearHelper.enchant(stack, ThemedGearHelper.unbreaking(), 3);
                ThemedGearHelper.enchant(stack, ThemedGearHelper.knockback(), 2);
            }));

    public static final RegistrySupplier<Item> LUCKY_MYSTERE = ITEMS.register("lucky_mystere", () ->
            new MysteryLuckyItem(props("lucky_mystere"), stack -> {
                ThemedGearHelper.name(stack, "Lucky Block Mystère");
                ThemedGearHelper.lore(stack, "Clic droit : 1 des 31 thèmes.", "Recharge 30s.");
            }));

    public static final RegistrySupplier<Item> JOURNAL_QUETES = ITEMS.register("journal_quetes", () ->
            new QuestJournalItem(props("journal_quetes"), stack -> {
                ThemedGearHelper.name(stack, "Journal des Quêtes");
                ThemedGearHelper.lore(stack, "Clic droit : ouvre le journal des quêtes.");
            }));

    public static final RegistrySupplier<Item> CARTE_COLLECTION = ITEMS.register("carte_collection", () ->
            new CollectionCardItem(props("carte_collection").stacksTo(64)));

    public static final RegistrySupplier<Item> ESPRESSO_ULTRA_DOSE = ITEMS.register("espresso_ultra_dose",
            () -> ExtendedModItems.espressoUltraDose(props("espresso_ultra_dose")));
    public static final RegistrySupplier<Item> VALISE_PERDUE = ITEMS.register("valise_perdue",
            () -> ExtendedModItems.valisePerdue(props("valise_perdue")));
    public static final RegistrySupplier<Item> TRANCHE_DEBAT = ITEMS.register("tranche_debat",
            () -> ExtendedModItems.trancheDebat(props("tranche_debat")));
    public static final RegistrySupplier<Item> CHRONO_WORLD_RECORD = ITEMS.register("chrono_world_record",
            () -> ExtendedModItems.chronoWorldRecord(props("chrono_world_record")));
    public static final RegistrySupplier<Item> DISQUE_DUR_CAPITAINE = ITEMS.register("disque_dur_capitaine",
            () -> ExtendedModItems.disqueDurCapitaine(props("disque_dur_capitaine")));
    public static final RegistrySupplier<Item> POP_IT_SUPREME = ITEMS.register("pop_it_supreme",
            () -> ExtendedModItems.popItSupreme(props("pop_it_supreme")));
    public static final RegistrySupplier<Item> FIOLE_SANG_NOBLE = ITEMS.register("fiole_sang_noble",
            () -> ExtendedModItems.fioleSangNoble(props("fiole_sang_noble")));
    public static final RegistrySupplier<Item> ZAPPER_DIMENSIONS = ITEMS.register("zapper_dimensions",
            () -> ExtendedModItems.zapperDimensions(props("zapper_dimensions")));

    private ModItems() {}

    static Item.Properties props(String path) {
        return new Item.Properties()
                .setId(ResourceKey.create(
                        Registries.ITEM,
                        Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, path)));
    }

    public static void register() {
        for (LuckyTheme theme : LuckyTheme.values()) {
            String path = "lucky_" + theme.id();
            ITEMS.register(path, () ->
                    new LuckyBlockItem(ModBlocks.get(theme), props(path)));
        }
        ITEMS.register();
    }
}
