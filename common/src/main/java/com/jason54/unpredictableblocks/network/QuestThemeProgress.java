package com.jason54.unpredictableblocks.network;

import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Progression d'un thème pour l'UI journal : quête (étapes), carte, maîtrise. */
public record QuestThemeProgress(
        String themeId,
        int rarity,
        boolean questDone,
        boolean masterDone,
        boolean hasCard,
        int breaks,
        int masterBreaks,
        List<Step> steps
) {
    /** Une étape de quête : critère (clé de texte) + progression « current/target ». */
    public record Step(String criterion, int current, int target) {
        public static final StreamCodec<ByteBuf, Step> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Step::criterion,
                ByteBufCodecs.VAR_INT, Step::current,
                ByteBufCodecs.VAR_INT, Step::target,
                Step::new
        );

        public boolean done() {
            return current >= target;
        }
    }

    public static final StreamCodec<ByteBuf, QuestThemeProgress> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, QuestThemeProgress::themeId,
            ByteBufCodecs.VAR_INT, QuestThemeProgress::rarity,
            ByteBufCodecs.BOOL, QuestThemeProgress::questDone,
            ByteBufCodecs.BOOL, QuestThemeProgress::masterDone,
            ByteBufCodecs.BOOL, QuestThemeProgress::hasCard,
            ByteBufCodecs.VAR_INT, QuestThemeProgress::breaks,
            ByteBufCodecs.VAR_INT, QuestThemeProgress::masterBreaks,
            Step.STREAM_CODEC.apply(ByteBufCodecs.list(8)), QuestThemeProgress::steps,
            QuestThemeProgress::new
    );

    /** Avancement 0..1 de la quête (moyenne des étapes). */
    public float questFraction() {
        if (questDone || steps.isEmpty()) {
            return questDone ? 1.0F : 0.0F;
        }
        float sum = 0;
        for (Step step : steps) {
            sum += Math.min(1.0F, step.current() / (float) Math.max(1, step.target()));
        }
        return sum / steps.size();
    }
}
