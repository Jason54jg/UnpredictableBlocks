package com.jason54.unpredictableblocks.quest;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import net.minecraft.world.level.block.Blocks;

/** Compteurs de quêtes pour les thèmes 25–32 (papier bulle, etc.). */
public final class QuestExtendedHandler {
    private QuestExtendedHandler() {}

    public static void register() {
        BlockEvent.BREAK.register((level, pos, state, player) -> {
            if (level.isClientSide()) {
                return EventResult.pass();
            }
            if (state.is(Blocks.HONEYCOMB_BLOCK) || state.is(Blocks.WHITE_WOOL)) {
                int n = PlayerQuestData.incrementCounter(player, "bubble_pop");
                if (n >= 50) {
                    QuestRewards.complete(player, "quest/asmr_fidget", "trigger_block");
                }
            }
            return EventResult.pass();
        });
    }
}
