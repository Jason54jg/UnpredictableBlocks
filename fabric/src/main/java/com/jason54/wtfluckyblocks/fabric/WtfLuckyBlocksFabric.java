package com.jason54.wtfluckyblocks.fabric;

import com.jason54.wtfluckyblocks.WtfLuckyBlocks;
import net.fabricmc.api.ModInitializer;

public final class WtfLuckyBlocksFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        WtfLuckyBlocks.init();
    }
}
