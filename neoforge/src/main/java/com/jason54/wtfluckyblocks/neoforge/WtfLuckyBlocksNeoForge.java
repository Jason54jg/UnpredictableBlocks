package com.jason54.wtfluckyblocks.neoforge;

import com.jason54.wtfluckyblocks.WtfLuckyBlocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(WtfLuckyBlocks.MOD_ID)
public final class WtfLuckyBlocksNeoForge {
    public WtfLuckyBlocksNeoForge(IEventBus modBus) {
        WtfLuckyBlocks.init();
    }
}
