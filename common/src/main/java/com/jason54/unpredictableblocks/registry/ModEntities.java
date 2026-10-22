package com.jason54.unpredictableblocks.registry;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.entity.LuckyRocketEntity;
import com.jason54.unpredictableblocks.entity.mob.ChatInternetEntity;
import com.jason54.unpredictableblocks.entity.mob.ChefItalienEntity;
import com.jason54.unpredictableblocks.entity.mob.ComteDraculaEntity;
import com.jason54.unpredictableblocks.entity.mob.CoqGauloisEntity;
import com.jason54.unpredictableblocks.entity.mob.EcranBleuEntity;
import com.jason54.unpredictableblocks.entity.mob.MascotteHamburgerEntity;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(UnpredictableBlocks.MOD_ID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<LuckyRocketEntity>> LUCKY_ROCKET =
            ENTITIES.register("lucky_rocket", () -> EntityType.Builder
                    .<LuckyRocketEntity>of(LuckyRocketEntity::new, MobCategory.MISC)
                    .noSave()
                    .noLootTable()
                    .fireImmune()
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(8)
                    .updateInterval(10)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, "lucky_rocket"))));

    // Mobs animés (modèles : tools/generate_mobs.py)
    public static final RegistrySupplier<EntityType<ChefItalienEntity>> CHEF_ITALIEN =
            mob(ChefItalienEntity.ID, ChefItalienEntity::new, MobCategory.MONSTER, 0.6F, 1.95F);
    public static final RegistrySupplier<EntityType<ComteDraculaEntity>> COMTE_DRACULA =
            mob(ComteDraculaEntity.ID, ComteDraculaEntity::new, MobCategory.MONSTER, 0.6F, 1.95F);
    public static final RegistrySupplier<EntityType<MascotteHamburgerEntity>> MASCOTTE_HAMBURGER =
            mob(MascotteHamburgerEntity.ID, MascotteHamburgerEntity::new, MobCategory.MONSTER, 1.0F, 1.4F);
    public static final RegistrySupplier<EntityType<ChatInternetEntity>> CHAT_INTERNET =
            mob(ChatInternetEntity.ID, ChatInternetEntity::new, MobCategory.CREATURE, 0.6F, 0.7F);
    public static final RegistrySupplier<EntityType<CoqGauloisEntity>> COQ_GAULOIS =
            mob(CoqGauloisEntity.ID, CoqGauloisEntity::new, MobCategory.CREATURE, 0.9F, 1.9F);
    public static final RegistrySupplier<EntityType<EcranBleuEntity>> ECRAN_BLEU =
            mob(EcranBleuEntity.ID, EcranBleuEntity::new, MobCategory.MONSTER, 1.1F, 0.9F);

    private ModEntities() {}

    private static <T extends Entity> RegistrySupplier<EntityType<T>> mob(
            String id, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height) {
        return ENTITIES.register(id, () -> EntityType.Builder.of(factory, category)
                .sized(width, height)
                .clientTrackingRange(8)
                .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, id))));
    }

    public static void register() {
        ENTITIES.register();
        EntityAttributeRegistry.register(CHEF_ITALIEN, ChefItalienEntity::createAttributes);
        EntityAttributeRegistry.register(COMTE_DRACULA, ComteDraculaEntity::createAttributes);
        EntityAttributeRegistry.register(MASCOTTE_HAMBURGER, MascotteHamburgerEntity::createAttributes);
        EntityAttributeRegistry.register(CHAT_INTERNET, ChatInternetEntity::createAttributes);
        EntityAttributeRegistry.register(COQ_GAULOIS, CoqGauloisEntity::createAttributes);
        EntityAttributeRegistry.register(ECRAN_BLEU, EcranBleuEntity::createAttributes);
    }
}
