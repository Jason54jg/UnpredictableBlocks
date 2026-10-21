package com.jason54.unpredictableblocks.neoforge.client;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = UnpredictableBlocks.MOD_ID, value = Dist.CLIENT)
public final class UnpredictableBlocksNeoForgeClient {
    private UnpredictableBlocksNeoForgeClient() {}

    @SubscribeEvent
    static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof TitleScreen title)) {
            return;
        }
        int left = title.width / 2 - 100;
        int top = title.height / 4 + 48 + 72 + 12;
        event.addListener(Button.builder(
                Component.literal("UB World profile"),
                b -> Minecraft.getInstance().setScreen(new WorldProfileCreateScreen(title))
        ).bounds(left, top, 200, 20).build());
    }
}
