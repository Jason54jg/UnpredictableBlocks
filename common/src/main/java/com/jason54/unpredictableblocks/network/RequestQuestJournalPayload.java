package com.jason54.unpredictableblocks.network;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** C2S : demande d'ouverture du journal (bouton inventaire). */
public record RequestQuestJournalPayload() implements CustomPacketPayload {
    public static final Type<RequestQuestJournalPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, "request_quest_journal"));

    public static final RequestQuestJournalPayload INSTANCE = new RequestQuestJournalPayload();

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestQuestJournalPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}