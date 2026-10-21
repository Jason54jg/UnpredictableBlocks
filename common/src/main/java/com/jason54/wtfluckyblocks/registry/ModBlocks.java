package com.jason54.wtfluckyblocks.registry;

import com.jason54.wtfluckyblocks.block.LuckyBlock;
import com.jason54.wtfluckyblocks.theme.LuckyTheme;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.EnumMap;
import java.util.Map;

public final class ModBlocks {
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(com.jason54.wtfluckyblocks.WtfLuckyBlocks.MOD_ID, Registries.BLOCK);

    private static final Map<LuckyTheme, RegistrySupplier<LuckyBlock>> BY_THEME = new EnumMap<>(LuckyTheme.class);

    private ModBlocks() {}

    public static void register() {
        for (LuckyTheme theme : LuckyTheme.values()) {
            RegistrySupplier<LuckyBlock> supplier = BLOCKS.register("lucky_" + theme.id(), () ->
                    new LuckyBlock(theme, BlockBehaviour.Properties.of()
                            .strength(0.5f)
                            .sound(SoundType.WOOL)
                            .noOcclusion()));
            BY_THEME.put(theme, supplier);
        }
        BLOCKS.register();
    }

    public static LuckyBlock get(LuckyTheme theme) {
        return BY_THEME.get(theme).get();
    }
}
