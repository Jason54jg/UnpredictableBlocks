package com.jason54.unpredictableblocks.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Method;

public final class UnpredictableBlocksFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof TitleScreen title)) {
                return;
            }
            int left = scaledWidth / 2 - 100;
            int top = scaledHeight / 4 + 48 + 72 + 12;
            Button button = Button.builder(
                    Component.literal("UB World profile"),
                    b -> client.setScreen(new WorldProfileCreateScreen(title))
            ).bounds(left, top, 200, 20).build();
            addRenderableWidget(screen, button);
        });
    }

    private static void addRenderableWidget(Screen screen, Button button) {
        try {
            Method add = null;
            for (Method candidate : Screen.class.getDeclaredMethods()) {
                if ("addRenderableWidget".equals(candidate.getName()) && candidate.getParameterCount() == 1) {
                    add = candidate;
                    break;
                }
            }
            if (add == null) {
                throw new NoSuchMethodException("addRenderableWidget");
            }
            add.setAccessible(true);
            add.invoke(screen, button);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not add title screen button", e);
        }
    }
}
