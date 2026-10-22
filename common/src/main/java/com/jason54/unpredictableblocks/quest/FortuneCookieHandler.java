package com.jason54.unpredictableblocks.quest;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.ChatEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Code fortune éphémère (Fortune Cookie, palier neutral). */
public final class FortuneCookieHandler {
    private FortuneCookieHandler() {}

    public static void register() {
        ChatEvent.RECEIVED.register((player, message) -> {
            if (player.level().isClientSide()) {
                return EventResult.pass();
            }
            String text = message.getString().trim();
            if (PlayerQuestData.tryClaimFortuneCode(player, text)) {
                player.giveExperiencePoints(500);
                Quests.complete(player, com.jason54.unpredictableblocks.theme.LuckyTheme.FORTUNE_COOKIE, "fortune_code");
                player.sendSystemMessage(Component.literal("§aFortune validée — +500 XP !"));
            }
            return EventResult.pass();
        });
    }
}
