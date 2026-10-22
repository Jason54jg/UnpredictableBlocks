package com.jason54.unpredictableblocks.fabric.client;

import com.jason54.unpredictableblocks.client.TitleScreenButtonLayout;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

import java.lang.reflect.Method;

public final class UnpredictableBlocksFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof TitleScreen title)) {
                return;
            }
            if (TitleScreenButtonLayout.alreadyPresent(screen.children())) {
                return;
            }
            int left = TitleScreenButtonLayout.left(scaledWidth);
            int top = TitleScreenButtonLayout.topBelowExisting(scaledWidth, scaledHeight, screen.children());
            Button button = Button.builder(
                    TitleScreenButtonLayout.LABEL,
                    b -> client.setScreenAndShow(new WorldProfileCreateScreen(title))
            ).bounds(left, top, TitleScreenButtonLayout.BUTTON_WIDTH, TitleScreenButtonLayout.BUTTON_HEIGHT).build();
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
