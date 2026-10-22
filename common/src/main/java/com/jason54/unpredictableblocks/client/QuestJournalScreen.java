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
    private static final int COLOR_TITLE = 0xFFE0E0E0;
    private static final int COLOR_DONE = 0xFF55FF55;
    private static final int COLOR_PENDING = 0xFFAAAAAA;
    private static final int COLOR_META = 0xFFC0C0C0;
    private static final int COLOR_LEGEND = 0xFFA0A0A0;

    private final OpenQuestJournalPayload payload;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 44, 36);
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

        LinearLayout header = layout.addToHeader(LinearLayout.vertical().spacing(4));
        header.defaultCellSetting().alignHorizontallyCenter();
        header.addChild(new StringWidget(TITLE, font));
        header.addChild(new StringWidget(summary, font));

        list = layout.addToContents(new ThemeList(minecraft, payload));

        layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, b -> onClose()).width(200).build());

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
                this.height - 12,
                COLOR_LEGEND
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
            return Math.min(360, width - 32);
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
                    g.item(new ItemStack(ModBlocks.get(theme)), x + 4, y + 6);
                }

                Component title = Component.translatable(
                        "advancements.unpredictableblocks." + progress.themeId() + ".title");
                int titleColor = progress.questDone() ? COLOR_DONE : COLOR_TITLE;
                g.text(font, title, x + 28, y + 3, titleColor);

                Component marks = Component.empty()
                        .append(progress.questDone()
                                ? Component.translatable("screen.unpredictableblocks.quest_journal.mark_quest_done")
                                : Component.translatable("screen.unpredictableblocks.quest_journal.mark_quest_pending"))
                        .append("  ")
                        .append(progress.hasCard()
                                ? Component.translatable("screen.unpredictableblocks.quest_journal.mark_card_done")
                                : Component.translatable("screen.unpredictableblocks.quest_journal.mark_card_pending"))
                        .append("  ")
                        .append(progress.masterDone()
                                ? Component.translatable("screen.unpredictableblocks.quest_journal.mark_master_done")
                                : Component.translatable("screen.unpredictableblocks.quest_journal.mark_master_pending"))
                        .append("  ")
                        .append(Component.translatable(
                                "screen.unpredictableblocks.quest_journal.breaks", progress.breaks()));
                g.text(font, marks, x + 28, y + 15, COLOR_META);
            }

            @Override
            public Component getNarration() {
                return Component.translatable(
                        "advancements.unpredictableblocks." + progress.themeId() + ".title");
            }
        }
    }
}