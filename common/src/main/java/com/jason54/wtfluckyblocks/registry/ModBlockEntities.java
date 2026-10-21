package com.jason54.wtfluckyblocks.registry;

import com.jason54.wtfluckyblocks.WtfLuckyBlocks;
import com.jason54.wtfluckyblocks.block.entity.LuckyBlockEntity;
import com.jason54.wtfluckyblocks.theme.LuckyTheme;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES =
            DeferredRegister.create(WtfLuckyBlocks.MOD_ID, Registries.BLOCK_ENTITY);

    public static final RegistrySupplier<BlockEntityType<LuckyBlockEntity>> LUCKY_BLOCK =
            ENTITIES.register("lucky_block", () -> {
                Block[] blocks = new Block[LuckyTheme.values().length];
                int i = 0;
                for (LuckyTheme theme : LuckyTheme.values()) {
                    blocks[i++] = ModBlocks.get(theme);
                }
                return BlockEntityType.Builder.of(LuckyBlockEntity::new, blocks).build(null);
            });

    public static void register() {
        ENTITIES.register();
    }
}
