package com.jason54.unpredictableblocks.neoforge.client;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.client.TitleScreenButtonLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
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
        if (TitleScreenButtonLayout.alreadyPresent(event.getListenersList())) {
            return;
        }
        int left = TitleScreenButtonLayout.left(title.width);
        int top = TitleScreenButtonLayout.topBelowExisting(title.width, title.height, event.getListenersList());
        event.addListener(Button.builder(
                TitleScreenButtonLayout.LABEL,
                b -> Minecraft.getInstance().setScreenAndShow(new WorldProfileCreateScreen(title))
        ).bounds(left, top, TitleScreenButtonLayout.BUTTON_WIDTH, TitleScreenButtonLayout.BUTTON_HEIGHT).build());
    }
}
