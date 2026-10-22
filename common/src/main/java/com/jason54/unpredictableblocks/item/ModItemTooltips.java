package com.jason54.unpredictableblocks.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

/**
 * Tooltips multi-lignes via cles :
 * {@code <id>.desc} / {@code .usage} / {@code .effect} / {@code .quest}
 */
public final class ModItemTooltips {
    private ModItemTooltips() {}

    public static void appendDescription(Item item, Consumer<Component> tooltip) {
        String base = item.getDescriptionId();
        appendIfPresent(tooltip, base + ".desc", ChatFormatting.GRAY);
        appendIfPresent(tooltip, base + ".usage", ChatFormatting.DARK_AQUA);
        appendIfPresent(tooltip, base + ".effect", ChatFormatting.YELLOW);
        appendIfPresent(tooltip, base + ".quest", ChatFormatting.GOLD);
    }

    public static void appendIfPresent(Consumer<Component> tooltip, String key, ChatFormatting style) {
        if (!Language.getInstance().has(key)) {
            return;
        }
        tooltip.accept(Component.translatable(key).withStyle(style));
    }

    public static void appendLine(Consumer<Component> tooltip, String key, ChatFormatting style, Object... args) {
        if (!Language.getInstance().has(key)) {
            return;
        }
        tooltip.accept(Component.translatable(key, args).withStyle(style));
    }
}