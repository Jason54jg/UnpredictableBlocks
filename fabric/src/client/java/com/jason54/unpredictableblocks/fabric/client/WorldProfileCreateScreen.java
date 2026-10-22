package com.jason54.unpredictableblocks.fabric.client;

import com.jason54.unpredictableblocks.world.NextWorldProfileStorage;
import com.jason54.unpredictableblocks.world.WorldPlayMode;
import com.jason54.unpredictableblocks.world.WorldProfileMenuCatalog;
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
        int y = 36;
        int w = 240;
        int x = width / 2 - w / 2;
        addRenderableWidget(Button.builder(Component.literal("Normal"), b -> pick(WorldPlayMode.NORMAL, 1)).bounds(x, y, w, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Easy"), b -> pick(WorldPlayMode.EASY, 1)).bounds(x, y, w, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Hard"), b -> pick(WorldPlayMode.HARD, 1)).bounds(x, y, w, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Hardcore"), b -> pick(WorldPlayMode.HARDCORE, 1)).bounds(x, y, w, 20).build());
        y += 28;
        addRenderableWidget(Button.builder(Component.literal("OneBlock (1–4 players)…"), b -> openList(
                Component.literal("OneBlock"),
                WorldProfileMenuCatalog.coreModes().stream().filter(e -> e.id().startsWith("oneblock_")).toList()
        )).bounds(x, y, w, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Parcours (1–4 players)…"), b -> openList(
                Component.literal("Parcours Lucky"),
                WorldProfileMenuCatalog.coreModes().stream().filter(e -> e.id().startsWith("parcours_")).toList()
        )).bounds(x, y, w, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Advanced presets (race, coop…)…"), b -> openList(
                Component.literal("Presets"),
                WorldProfileMenuCatalog.presets()
        )).bounds(x, y, w, 20).build());
        y += 28;
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose()).bounds(x, y, w, 20).build());
    }

    private void openList(Component title, java.util.List<WorldProfileMenuCatalog.Entry> entries) {
        if (minecraft != null) {
            minecraft.setScreenAndShow(new WorldProfileListScreen(
                    this,
                    title,
                    Component.literal("Click to queue — then Create World"),
                    entries
            ));
        }
    }

    private void pick(WorldPlayMode mode, int players) {
        NextWorldProfileStorage.save(new NextWorldProfileStorage.Pending(mode, players));
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
        graphics.centeredText(font, Component.literal("Pick a mode before Create World"), width / 2, 24, 0xAAAAAA);
    }
}
