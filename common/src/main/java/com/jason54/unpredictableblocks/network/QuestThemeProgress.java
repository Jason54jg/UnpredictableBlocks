package com.jason54.unpredictableblocks.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Progression d'un theme pour l'UI journal. */
public record QuestThemeProgress(
        String themeId,
        boolean questDone,
        boolean masterDone,
        boolean hasCard,
        int breaks
) {
    public static final StreamCodec<ByteBuf, QuestThemeProgress> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, QuestThemeProgress::themeId,
            ByteBufCodecs.BOOL, QuestThemeProgress::questDone,
            ByteBufCodecs.BOOL, QuestThemeProgress::masterDone,
            ByteBufCodecs.BOOL, QuestThemeProgress::hasCard,
            ByteBufCodecs.VAR_INT, QuestThemeProgress::breaks,
            QuestThemeProgress::new
    );
}