package com.jason54.unpredictableblocks.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

/** MC 26.1 removed {@code BlockEntityType.Builder}; construct via accessible constructor. */
public final class BlockEntityTypeBuilder {
    private BlockEntityTypeBuilder() {}

    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> BlockEntityType<T> of(
            BlockEntityType.BlockEntitySupplier<? extends T> factory,
            Block... blocks
    ) {
        try {
            var constructor = BlockEntityType.class.getDeclaredConstructor(
                    BlockEntityType.BlockEntitySupplier.class,
                    Set.class
            );
            constructor.setAccessible(true);
            return (BlockEntityType<T>) constructor.newInstance(factory, Set.of(blocks));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create BlockEntityType", e);
        }
    }
}
