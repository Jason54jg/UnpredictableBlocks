package com.jason54.unpredictableblocks.registry;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.block.LuckyBlock;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.EnumMap;
import java.util.Map;

public final class ModBlocks {
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(UnpredictableBlocks.MOD_ID, Registries.BLOCK);

    private static final Map<LuckyTheme, RegistrySupplier<LuckyBlock>> BY_THEME = new EnumMap<>(LuckyTheme.class);

    private ModBlocks() {}

    public static void register() {
        for (LuckyTheme theme : LuckyTheme.values()) {
            String path = "lucky_" + theme.id();
            RegistrySupplier<LuckyBlock> supplier = BLOCKS.register(path, () ->
                    new LuckyBlock(theme, BlockBehaviour.Properties.of()
                            .strength(0.5f)
                            .sound(SoundType.WOOL)
                            .noOcclusion()
                            .setId(ResourceKey.create(Registries.BLOCK, theme.blockId()))));
            BY_THEME.put(theme, supplier);
        }
        BLOCKS.register();
    }

    public static LuckyBlock get(LuckyTheme theme) {
        return BY_THEME.get(theme).get();
    }
}
