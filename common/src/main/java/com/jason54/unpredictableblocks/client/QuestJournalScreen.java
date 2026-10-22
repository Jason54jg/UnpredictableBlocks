package com.jason54.unpredictableblocks.client;

import com.jason54.unpredictableblocks.network.OpenQuestJournalPayload;
import com.jason54.unpredictableblocks.network.QuestThemeProgress;
import com.jason54.unpredictableblocks.registry.ModBlocks;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Journal des quêtes : onglets (toutes / par rareté / à faire), liste des thèmes à gauche avec barre
 * d'avancement, détail du thème sélectionné à droite (objectifs « 3/10 », indice, carte, maîtrise).
 * La quête conseillée par le serveur est marquée d'une étoile et sélectionnée à l'ouverture.
 */
public final class QuestJournalScreen extends Screen {
    private static final String KEY = "screen.unpredictableblocks.quest_journal";
    private static final Component TITLE = Component.translatable(KEY);

    private static final int COLOR_TEXT = 0xFFE0E0E0;
    private static final int COLOR_DIM = 0xFFA0A0A0;
    private static final int COLOR_DONE = 0xFF55FF55;
    private static final int COLOR_SUGGESTED = 0xFFFFD34D;
    private static final int COLOR_PANEL = 0xC0101018;
    private static final int COLOR_PANEL_EDGE = 0xFF4A4A5A;
    private static final int COLOR_BAR_BG = 0xFF2A2A2A;
    private static final int[] RARITY_COLORS = {0xFFB0B0B0, 0xFF55AAFF, 0xFFFFAA00};

    private static final int TOP = 58;
    private static final int TAB_Y = 32;
    private static final int BOTTOM = 32;

    private enum Tab {
        ALL(t -> true),
        COMMON(t -> t.rarity() == 0),
        RARE(t -> t.rarity() == 1),
        LEGENDARY(t -> t.rarity() == 2),
        TODO(t -> !t.questDone());

        final Predicate<QuestThemeProgress> filter;

        Tab(Predicate<QuestThemeProgress> filter) {
            this.filter = filter;
        }
    }

    private final OpenQuestJournalPayload payload;
    private final List<Button> tabButtons = new ArrayList<>();
    private Tab tab = Tab.ALL;
    private ThemeList list;
    private QuestThemeProgress selected;

    public QuestJournalScreen(OpenQuestJournalPayload payload) {
        super(TITLE);
        this.payload = payload;
        for (QuestThemeProgress theme : payload.themes()) {
            if (theme.themeId().equals(payload.suggestedThemeId())) {
                selected = theme;
            }
        }
        if (selected == null && !payload.themes().isEmpty()) {
            selected = payload.themes().getFirst();
        }
    }

    @Override
    protected void init() {
        tabButtons.clear();
        int tabWidth = Math.min(80, (width - 40) / Tab.values().length);
        int tabsX = (width - tabWidth * Tab.values().length) / 2;
        for (Tab t : Tab.values()) {
            Button button = Button.builder(tabLabel(t), b -> selectTab(t))
                    .bounds(tabsX + t.ordinal() * tabWidth, TAB_Y, tabWidth - 2, 20)
                    .build();
            tabButtons.add(addRenderableWidget(button));
        }

        list = addRenderableWidget(new ThemeList(minecraft));
        list.updateSizeAndPosition(listWidth(), height - TOP - BOTTOM, 8, TOP);
        selectTab(tab);

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
                .bounds(width / 2 - 100, height - 26, 200, 20)
                .build());
    }

    private int listWidth() {
        return Math.min(220, width / 2 - 12);
    }

    private Component tabLabel(Tab t) {
        long count = payload.themes().stream().filter(t.filter).count();
        return Component.translatable(KEY + ".tab." + t.name().toLowerCase(java.util.Locale.ROOT), count);
    }

    private void selectTab(Tab t) {
        tab = t;
        for (int i = 0; i < tabButtons.size(); i++) {
            tabButtons.get(i).active = i != t.ordinal();
        }
        list.show(payload.themes().stream().filter(t.filter).toList());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        g.centeredText(font, TITLE, width / 2, 8, COLOR_TEXT);
        int total = payload.themes().size();
        g.centeredText(font, Component.translatable(KEY + ".summary",
                payload.questCount(), total, payload.cardCount(), payload.masterCount()), width / 2, 20, COLOR_DIM);
        extractDetail(g);
    }

    // ------------------------------------------------------------------ panneau détail

    private void extractDetail(GuiGraphicsExtractor g) {
        int x0 = 8 + listWidth() + 8;
        int x1 = width - 8;
        int y0 = TOP;
        int y1 = height - BOTTOM;
        g.fill(x0, y0, x1, y1, COLOR_PANEL);
        g.outline(x0, y0, x1 - x0, y1 - y0, COLOR_PANEL_EDGE);
        if (selected == null) {
            g.centeredText(font, Component.translatable(KEY + ".empty"), (x0 + x1) / 2, (y0 + y1) / 2, COLOR_DIM);
            return;
        }
        QuestThemeProgress t = selected;
        int x = x0 + 8;
        int w = x1 - x0 - 16;
        int y = y0 + 8;

        LuckyTheme theme = themeOf(t.themeId());
        if (theme != null) {
            g.item(new ItemStack(ModBlocks.get(theme)), x, y);
        }
        g.text(font, questTitle(t), x + 22, y, t.questDone() ? COLOR_DONE : COLOR_TEXT);
        Component rarity = Component.translatable(KEY + ".rarity." + t.rarity());
        g.text(font, rarity, x + 22, y + 10, RARITY_COLORS[Math.clamp(t.rarity(), 0, 2)]);
        if (t.themeId().equals(payload.suggestedThemeId())) {
            g.text(font, Component.translatable(KEY + ".suggested"), x + 26 + font.width(rarity), y + 10, COLOR_SUGGESTED);
        }
        y += 26;

        g.text(font, Component.translatable(KEY + ".objectives"), x, y, COLOR_SUGGESTED);
        y += 12;
        for (QuestThemeProgress.Step step : t.steps()) {
            boolean done = step.done() || t.questDone();
            Component line = Component.literal(done ? "✔ " : "○ ")
                    .append(Component.translatable("quest.unpredictableblocks." + t.themeId() + "." + step.criterion()));
            g.textWithWordWrap(font, line, x, y, w - 46, done ? COLOR_DONE : COLOR_TEXT);
            int lineHeight = font.wordWrapHeight(line, w - 46);
            if (step.target() > 1) {
                String count = Math.min(step.current(), step.target()) + "/" + step.target();
                g.text(font, count, x + w - font.width(count), y, done ? COLOR_DONE : COLOR_DIM);
                bar(g, x + 10, y + lineHeight + 1, w - 10, step.current() / (float) step.target(), done);
                lineHeight += 5;
            }
            y += lineHeight + 4;
        }
        if (t.questDone()) {
            g.text(font, Component.translatable(KEY + ".quest_done"), x, y, COLOR_DONE);
            y += 12;
        }
        y += 4;

        if (!t.questDone()) {
            g.text(font, Component.translatable(KEY + ".hint"), x, y, COLOR_SUGGESTED);
            y += 12;
            Component hint = Component.translatable("quest.unpredictableblocks." + t.themeId() + ".hint");
            g.textWithWordWrap(font, hint, x, y, w, COLOR_DIM);
            y += font.wordWrapHeight(hint, w) + 8;
        }

        g.text(font, Component.translatable(t.hasCard() ? KEY + ".card_done" : KEY + ".card_pending"),
                x, y, t.hasCard() ? COLOR_DONE : COLOR_DIM);
        y += 12;
        Component master = t.masterDone()
                ? Component.translatable(KEY + ".master_done")
                : Component.translatable(KEY + ".master", Math.min(t.breaks(), t.masterBreaks()), t.masterBreaks());
        g.text(font, master, x, y, t.masterDone() ? COLOR_DONE : COLOR_DIM);
        if (!t.masterDone()) {
            bar(g, x, y + 10, w, t.breaks() / (float) Math.max(1, t.masterBreaks()), false);
        }
    }

    private static void bar(GuiGraphicsExtractor g, int x, int y, int w, float fraction, boolean done) {
        g.fill(x, y, x + w, y + 3, COLOR_BAR_BG);
        int filled = Math.round(w * Math.clamp(fraction, 0.0F, 1.0F));
        if (filled > 0) {
            g.fill(x, y, x + filled, y + 3, done ? COLOR_DONE : COLOR_SUGGESTED);
        }
    }

    private static Component questTitle(QuestThemeProgress t) {
        return Component.translatable("advancements.unpredictableblocks." + t.themeId() + ".title");
    }

    private static LuckyTheme themeOf(String id) {
        for (LuckyTheme theme : LuckyTheme.values()) {
            if (theme.id().equals(id)) {
                return theme;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ liste des thèmes

    private final class ThemeList extends ObjectSelectionList<ThemeList.Entry> {
        ThemeList(Minecraft mc) {
            super(mc, listWidth(), QuestJournalScreen.this.height - TOP - BOTTOM, TOP, 30);
        }

        void show(List<QuestThemeProgress> themes) {
            List<Entry> entries = new ArrayList<>(themes.size());
            Entry toSelect = null;
            for (QuestThemeProgress theme : themes) {
                Entry entry = new Entry(theme);
                entries.add(entry);
                if (theme == selected) {
                    toSelect = entry;
                }
            }
            replaceEntries(entries);
            setScrollAmount(0);
            if (toSelect == null && !entries.isEmpty()) {
                toSelect = entries.getFirst();
            }
            setSelected(toSelect);
        }

        @Override
        public void setSelected(Entry entry) {
            super.setSelected(entry);
            selected = entry == null ? null : entry.progress;
        }

        @Override
        public int getRowWidth() {
            return width - 14;
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
                int w = getContentWidth();
                LuckyTheme theme = themeOf(progress.themeId());
                if (theme != null) {
                    g.item(new ItemStack(ModBlocks.get(theme)), x + 2, y + 5);
                }
                boolean suggested = progress.themeId().equals(payload.suggestedThemeId());
                Component title = suggested ? Component.literal("★ ").append(questTitle(progress)) : questTitle(progress);
                int color = progress.questDone() ? COLOR_DONE : suggested ? COLOR_SUGGESTED : COLOR_TEXT;
                g.text(font, font.substrByWidth(title, w - 60).getString(), x + 22, y + 3, color);

                String marks = (progress.questDone() ? "Q" : "·") + " " + (progress.hasCard() ? "C" : "·") + " "
                        + (progress.masterDone() ? "M" : "·");
                g.text(font, marks, x + w - font.width(marks) - 2, y + 3, RARITY_COLORS[Math.clamp(progress.rarity(), 0, 2)]);
                bar(g, x + 22, y + 16, w - 26, progress.questFraction(), progress.questDone());
            }

            @Override
            public Component getNarration() {
                return questTitle(progress);
            }
        }
    }
}
