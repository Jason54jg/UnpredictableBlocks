package com.jason54.unpredictableblocks.registry;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.block.entity.LuckyBlockEntity;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES =
            DeferredRegister.create(UnpredictableBlocks.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<LuckyBlockEntity>> LUCKY_BLOCK =
            ENTITIES.register("lucky_block", () -> {
                Block[] blocks = new Block[LuckyTheme.values().length];
                int i = 0;
                for (LuckyTheme theme : LuckyTheme.values()) {
                    blocks[i++] = ModBlocks.get(theme);
                }
                return BlockEntityTypeBuilder.of(LuckyBlockEntity::new, blocks);
            });

    public static void register() {
        ENTITIES.register();
    }
}
