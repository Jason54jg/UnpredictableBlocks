package com.jason54.unpredictableblocks.event;

import com.jason54.unpredictableblocks.config.ModConfig;
import com.jason54.unpredictableblocks.util.ServerLevels;
import com.jason54.unpredictableblocks.world.ChunkLuckAura;
import com.jason54.unpredictableblocks.item.LuckyBlockItem;
import com.jason54.unpredictableblocks.luck.PlayerLuckStorage;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Barre d'action : modificateur Luck joueur (+ item en main). */
public final class LuckHudHandler {
    private LuckHudHandler() {}

    public static void register() {
        TickEvent.PLAYER_POST.register(player -> {
            if (player.level().isClientSide() || !(player instanceof ServerPlayer sp)) {
                return;
            }
            if (!ModConfig.get().meta().showLuckHudOrDefault()) {
                return;
            }
            if (sp.tickCount % 20 != 0) {
                return;
            }
            int modifier = PlayerLuckStorage.getModifier(sp).value();
            ItemStack main = sp.getMainHandItem();
            String held = "";
            if (main.getItem() instanceof LuckyBlockItem) {
                int blockLuck = LuckyBlockItem.readLuckFromStack(main);
                if (blockLuck != 0) {
                    held = " §8| §fBloc §b" + formatLuck(blockLuck);
                }
            }
            String aura = ChunkLuckAura.hudSuffix(ServerLevels.of(sp), sp.blockPosition());
            sp.sendOverlayMessage(Component.literal("§bLuck §f" + formatLuck(modifier) + held + aura));
        });
    }

    private static String formatLuck(int value) {
        return value > 0 ? "+" + value : String.valueOf(value);
    }
}
