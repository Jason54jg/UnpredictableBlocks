package com.jason54.unpredictableblocks.registry;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.entity.LuckyRocketEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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

    private ModEntities() {}

    public static void register() {
        ENTITIES.register();
    }
}
