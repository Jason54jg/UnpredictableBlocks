package com.jason54.unpredictableblocks.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

public final class UnpredictableBlocksFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.afterInit((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof TitleScreen title)) {
                return;
            }
            int left = scaledWidth / 2 - 100;
            int top = scaledHeight / 4 + 48 + 72 + 12;
            Button button = Button.builder(
                    Component.literal("UB World profile"),
                    b -> client.setScreen(new WorldProfileCreateScreen(title))
            ).bounds(left, top, 200, 20).build();
            Screens.addButton(screen, button);
        });
    }
}
