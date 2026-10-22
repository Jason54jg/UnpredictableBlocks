package com.jason54.unpredictableblocks.client;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

/** Placement du bouton profil monde sous la derniere rangee du menu titre. */
public final class TitleScreenButtonLayout {
    public static final Component LABEL = Component.literal("UB World profile");
    public static final int BUTTON_WIDTH = 200;
    public static final int BUTTON_HEIGHT = 20;
    private static final int GAP = 4;

    private TitleScreenButtonLayout() {}

    public static int left(int screenWidth) {
        return screenWidth / 2 - BUTTON_WIDTH / 2;
    }

    /**
     * Y juste sous le bouton le plus bas deja present (Mods / Options / Quit),
     * pour eviter le chevauchement avec le layout NeoForge/Fabric.
     */
    public static int topBelowExisting(int screenWidth, int screenHeight, Iterable<? extends GuiEventListener> listeners) {
        int maxBottom = screenHeight / 4 + 48 + 96;
        for (GuiEventListener listener : listeners) {
            if (!(listener instanceof AbstractWidget widget) || !widget.visible) {
                continue;
            }
            if (isOurButton(widget)) {
                continue;
            }
            int centerX = widget.getX() + widget.getWidth() / 2;
            if (Math.abs(centerX - screenWidth / 2) > 130) {
                continue;
            }
            maxBottom = Math.max(maxBottom, widget.getY() + widget.getHeight());
        }
        return maxBottom + GAP;
    }

    public static boolean alreadyPresent(Iterable<? extends GuiEventListener> listeners) {
        for (GuiEventListener listener : listeners) {
            if (listener instanceof AbstractWidget widget && isOurButton(widget)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOurButton(AbstractWidget widget) {
        return LABEL.getString().equals(widget.getMessage().getString());
    }
}