package com.jason54.unpredictableblocks.network;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** S2C : ouvre le journal des quêtes côté client ; {@code suggestedThemeId} = prochaine quête conseillée ("" si tout est fait). */
public record OpenQuestJournalPayload(List<QuestThemeProgress> themes, String suggestedThemeId)
        implements CustomPacketPayload {
    public static final Type<OpenQuestJournalPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, "open_quest_journal"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenQuestJournalPayload> STREAM_CODEC =
            StreamCodec.composite(
                    QuestThemeProgress.STREAM_CODEC.apply(ByteBufCodecs.list(64)),
                    OpenQuestJournalPayload::themes,
                    ByteBufCodecs.STRING_UTF8,
                    OpenQuestJournalPayload::suggestedThemeId,
                    OpenQuestJournalPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public int questCount() {
        int n = 0;
        for (QuestThemeProgress t : themes) {
            if (t.questDone()) {
                n++;
            }
        }
        return n;
    }

    public int cardCount() {
        int n = 0;
        for (QuestThemeProgress t : themes) {
            if (t.hasCard()) {
                n++;
            }
        }
        return n;
    }

    public int masterCount() {
        int n = 0;
        for (QuestThemeProgress t : themes) {
            if (t.masterDone()) {
                n++;
            }
        }
        return n;
    }
}
