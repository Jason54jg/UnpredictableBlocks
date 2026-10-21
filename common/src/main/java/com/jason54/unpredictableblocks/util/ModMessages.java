package com.jason54.unpredictableblocks.util;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import net.minecraft.network.chat.Component;

/** Clés {@code message.unpredictableblocks.*} (en_us / fr_fr / en_gb). */
public final class ModMessages {
    private ModMessages() {}

    public static Component msg(String suffix, Object... args) {
        return Component.translatable("message." + UnpredictableBlocks.MOD_ID + "." + suffix, args);
    }
}
