package com.jason54.unpredictableblocks.neoforge.client;

import com.jason54.unpredictableblocks.world.NextWorldProfileStorage;
import com.jason54.unpredictableblocks.world.WorldPlayMode;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Choix du profil mod avant création du monde (solo). */
public class WorldProfileCreateScreen extends Screen {
    private final Screen parent;

    public WorldProfileCreateScreen(Screen parent) {
        super(Component.literal("UnpredictableBlocks — World profile"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int y = 40;
        int w = 220;
        int x = this.width / 2 - w / 2;
        addRenderableWidget(Button.builder(Component.literal("Normal"), b -> pick(WorldPlayMode.NORMAL, 1)).bounds(x, y, w, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Easy"), b -> pick(WorldPlayMode.EASY, 1)).bounds(x, y, w, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Hard"), b -> pick(WorldPlayMode.HARD, 1)).bounds(x, y, w, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Hardcore"), b -> pick(WorldPlayMode.HARDCORE, 1)).bounds(x, y, w, 20).build());
        y += 32;
        addRenderableWidget(Button.builder(Component.literal("OneBlock — 1 player"), b -> pick(WorldPlayMode.ONEBLOCK, 1)).bounds(x, y, w, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("OneBlock — 2 players"), b -> pick(WorldPlayMode.ONEBLOCK, 2)).bounds(x, y, w, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("OneBlock — 3 players"), b -> pick(WorldPlayMode.ONEBLOCK, 3)).bounds(x, y, w, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("OneBlock — 4 players"), b -> pick(WorldPlayMode.ONEBLOCK, 4)).bounds(x, y, w, 20).build());
        y += 32;
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose()).bounds(x, y, w, 20).build());
    }

    private void pick(WorldPlayMode mode, int players) {
        NextWorldProfileStorage.save(new NextWorldProfileStorage.Pending(mode, players));
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.centeredText(font, title, width / 2, 15, 0xFFFFFF);
        graphics.centeredText(
                font,
                Component.literal("Pick a mode before Create World"),
                width / 2,
                28,
                0xAAAAAA
        );
    }
}
