package com.jason54.unpredictableblocks.client;

import com.jason54.unpredictableblocks.client.geo.LuckyBlockRenderer;
import com.jason54.unpredictableblocks.client.geo.LuckyRocketRenderer;
import com.jason54.unpredictableblocks.network.OpenQuestJournalPayload;
import com.jason54.unpredictableblocks.network.RequestQuestJournalPayload;
import com.jason54.unpredictableblocks.registry.ModBlockEntities;
import com.jason54.unpredictableblocks.registry.ModEntities;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

/** Initialisation client : packets S2C, BER GeckoLib, bouton inventaire. */
public final class UnpredictableBlocksClient {
    private UnpredictableBlocksClient() {}

    public static void init() {
        BlockEntityRendererRegistry.register(ModBlockEntities.LUCKY_BLOCK.get(), LuckyBlockRenderer::new);
        EntityRendererRegistry.register(ModEntities.LUCKY_ROCKET, LuckyRocketRenderer::new);

        NetworkManager.registerReceiver(
                NetworkManager.s2c(),
                OpenQuestJournalPayload.TYPE,
                OpenQuestJournalPayload.STREAM_CODEC,
                (payload, context) -> context.queue(() ->
                        Minecraft.getInstance().setScreenAndShow(new QuestJournalScreen(payload)))
        );

        ClientGuiEvent.INIT_POST.register((screen, access) -> {
            if (!(screen instanceof InventoryScreen) && !(screen instanceof CreativeModeInventoryScreen)) {
                return;
            }
            int x = screen.width / 2 + 88;
            int y = screen.height / 2 - 76;
            if (screen instanceof CreativeModeInventoryScreen) {
                y = 6;
                x = screen.width / 2 + 70;
            }
            access.addRenderableWidget(Button.builder(
                            Component.translatable("screen.unpredictableblocks.quest_journal.open"),
                            b -> NetworkManager.sendToServer(RequestQuestJournalPayload.INSTANCE)
                    )
                    .bounds(x, y, 52, 20)
                    .build());
        });
    }
}
