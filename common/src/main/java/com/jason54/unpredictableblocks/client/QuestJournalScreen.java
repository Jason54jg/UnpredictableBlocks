package com.jason54.unpredictableblocks.client;

import com.jason54.unpredictableblocks.network.OpenQuestJournalPayload;
import com.jason54.unpredictableblocks.network.QuestThemeProgress;
import com.jason54.unpredictableblocks.registry.ModBlocks;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** UI journal des quetes (liste scrollable). */
public final class QuestJournalScreen extends Screen {
    private static final Component TITLE = Component.translatable("screen.unpredictableblocks.quest_journal");

    private final OpenQuestJournalPayload payload;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private ThemeList list;

    public QuestJournalScreen(OpenQuestJournalPayload payload) {
        super(TITLE);
        this.payload = payload;
    }

    @Override
    protected void init() {
        int total = payload.themes().size();
        var summary = Component.translatable(
                "screen.unpredictableblocks.quest_journal.summary",
                payload.questCount(),
                total,
                payload.cardCount(),
                payload.masterCount()
        );
        if (payload.casinoDone()) {
            summary.append(Component.literal("  \u00a76\u2605 casino"));
        }

        layout.addTitleHeader(TITLE, font);
        layout.addToHeader(new StringWidget(summary, font), settings -> settings.alignHorizontallyCenter());

        list = layout.addToContents(new ThemeList(minecraft, payload));

        LinearLayout footer = layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(Button.builder(CommonComponents.GUI_DONE, b -> onClose()).width(200).build());

        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
        if (list != null) {
            list.updateSize(width, layout);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(
                font,
                Component.translatable("screen.unpredictableblocks.quest_journal.legend"),
                width / 2,
                height - 18,
                0xA0A0A0
        );
    }

    private static LuckyTheme themeOf(String id) {
        for (LuckyTheme theme : LuckyTheme.values()) {
            if (theme.id().equals(id)) {
                return theme;
            }
        }
        return null;
    }

    private final class ThemeList extends ObjectSelectionList<ThemeList.Entry> {
        ThemeList(Minecraft mc, OpenQuestJournalPayload data) {
            super(mc, QuestJournalScreen.this.width, QuestJournalScreen.this.height - 96, 48, 28);
            for (QuestThemeProgress progress : data.themes()) {
                addEntry(new Entry(progress));
            }
        }

        @Override
        public int getRowWidth() {
            return Math.min(340, width - 40);
        }

        final class Entry extends ObjectSelectionList.Entry<Entry> {
            private final QuestThemeProgress progress;

            Entry(QuestThemeProgress progress) {
                this.progress = progress;
            }

            @Override
            public void extractContent(GuiGraphicsExtractor g, int mouseX, int mouseY, boolean hovered, float pt) {
                int x = getContentX();
                int y = getContentY();
                LuckyTheme theme = themeOf(progress.themeId());
                if (theme != null) {
                    g.item(new ItemStack(ModBlocks.get(theme)), x + 2, y + 4);
                }

                Component title = Component.translatable(
                        "advancements.unpredictableblocks." + progress.themeId() + ".title");
                int titleColor = progress.questDone() ? 0x55FF55 : 0xE0E0E0;
                g.text(font, title, x + 24, y + 2, titleColor, false);

                String marks = (progress.questDone() ? "\u00a7a\u2714" : "\u00a77\u25cb")
                        + (progress.hasCard() ? " \u00a7b\u25c6" : " \u00a78\u25c7")
                        + (progress.masterDone() ? " \u00a76\u2605" : " \u00a78\u2606")
                        + " \u00a78\u00b7 "
                        + Component.translatable(
                                "screen.unpredictableblocks.quest_journal.breaks", progress.breaks()).getString();
                g.text(font, marks, x + 24, y + 14, 0xA0A0A0, false);
            }

            @Override
            public Component getNarration() {
                return Component.translatable(
                        "advancements.unpredictableblocks." + progress.themeId() + ".title");
            }
        }
    }
}