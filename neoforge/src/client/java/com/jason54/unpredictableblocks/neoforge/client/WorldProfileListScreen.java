package com.jason54.unpredictableblocks.neoforge.client;

import com.jason54.unpredictableblocks.world.NextWorldProfileStorage;
import com.jason54.unpredictableblocks.world.WorldProfileMenuCatalog;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class WorldProfileListScreen extends Screen {
    private final Screen parent;
    private final Component subtitle;
    private final List<WorldProfileMenuCatalog.Entry> entries;
    private int scroll;

    public WorldProfileListScreen(Screen parent, Component title, Component subtitle, List<WorldProfileMenuCatalog.Entry> entries) {
        super(title);
        this.parent = parent;
        this.subtitle = subtitle;
        this.entries = entries;
    }

    @Override
    protected void init() {
        clearWidgets();
        int w = 240;
        int x = width / 2 - w / 2;
        int top = 42;
        int row = 22;
        int visible = Math.max(4, (height - top - 50) / row);
        int maxScroll = Math.max(0, entries.size() - visible);
        scroll = Math.min(scroll, maxScroll);

        if (scroll > 0) {
            addRenderableWidget(Button.builder(Component.literal("▲"), b -> {
                scroll = Math.max(0, scroll - 1);
                init();
            }).bounds(x + w - 22, top - 18, 20, 16).build());
        }
        if (scroll < maxScroll) {
            addRenderableWidget(Button.builder(Component.literal("▼"), b -> {
                scroll = Math.min(maxScroll, scroll + 1);
                init();
            }).bounds(x + w - 22, top + visible * row, 20, 16).build());
        }

        for (int i = 0; i < visible; i++) {
            int idx = scroll + i;
            if (idx >= entries.size()) {
                break;
            }
            WorldProfileMenuCatalog.Entry entry = entries.get(idx);
            int y = top + i * row;
            addRenderableWidget(Button.builder(Component.literal(entry.label()), b -> {
                NextWorldProfileStorage.save(entry.pending());
                if (minecraft != null) {
                    // Remonte au TitleScreen (parent de WorldProfileCreateScreen).
                    Screen root = parent instanceof WorldProfileCreateScreen create ? create.parentScreen() : parent;
                    minecraft.setScreenAndShow(root);
                }
            }).bounds(x, y, w, 20).build());
        }

        addRenderableWidget(Button.builder(Component.literal("Back"), b -> {
            if (minecraft != null) {
                minecraft.setScreenAndShow(parent);
            }
        }).bounds(x, height - 28, w, 20).build());
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreenAndShow(parent);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.centeredText(font, title, width / 2, 12, 0xFFFFFF);
        graphics.centeredText(font, subtitle, width / 2, 26, 0xAAAAAA);
    }
}
