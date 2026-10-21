package com.jason54.unpredictableblocks.neoforge;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(UnpredictableBlocks.MOD_ID)
public final class UnpredictableBlocksNeoForge {
    public UnpredictableBlocksNeoForge(IEventBus modBus) {
        UnpredictableBlocks.init();
    }
}
