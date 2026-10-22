package com.jason54.unpredictableblocks.network;

import com.jason54.unpredictableblocks.quest.QuestJournal;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerPlayer;

public final class ModNetworking {
    private ModNetworking() {}

    public static void register() {
        // S2C : type seul ici ; le receiver client est enregistre dans UnpredictableBlocksClient.
        NetworkManager.registerS2CPayloadType(OpenQuestJournalPayload.TYPE, OpenQuestJournalPayload.STREAM_CODEC);

        NetworkManager.registerC2S(
                RequestQuestJournalPayload.TYPE,
                RequestQuestJournalPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.getPlayer() instanceof ServerPlayer sp) {
                        context.queue(() -> QuestJournal.open(sp));
                    }
                });
    }
}