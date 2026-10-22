package com.jason54.unpredictableblocks.network;

import com.jason54.unpredictableblocks.quest.QuestJournal;
import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.server.level.ServerPlayer;

public final class ModNetworking {
    private ModNetworking() {}

    public static void register() {
        // Sur client, registerReceiver (UnpredictableBlocksClient) enregistre deja le type.
        // Sur serveur dedie, il faut le type seul pour pouvoir envoyer.
        EnvExecutor.runInEnv(Env.SERVER, () -> () ->
                NetworkManager.registerS2CPayloadType(
                        OpenQuestJournalPayload.TYPE,
                        OpenQuestJournalPayload.STREAM_CODEC));

        // registerReceiver(c2s) existe dans Architectury 20.x (26.1.2) comme 21/22 ; registerC2S non.
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                RequestQuestJournalPayload.TYPE,
                RequestQuestJournalPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.getPlayer() instanceof ServerPlayer sp) {
                        context.queue(() -> QuestJournal.open(sp));
                    }
                });
    }
}