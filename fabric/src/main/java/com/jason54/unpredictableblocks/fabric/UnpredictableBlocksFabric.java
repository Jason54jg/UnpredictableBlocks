package com.jason54.unpredictableblocks.fabric;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import net.fabricmc.api.ModInitializer;

public final class UnpredictableBlocksFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        UnpredictableBlocks.init();
    }
}
