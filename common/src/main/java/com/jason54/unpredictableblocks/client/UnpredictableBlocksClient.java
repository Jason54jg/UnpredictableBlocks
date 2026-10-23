package com.jason54.unpredictableblocks.client;

import com.jason54.unpredictableblocks.client.geo.LuckyBlockRenderer;
import com.jason54.unpredictableblocks.client.geo.LuckyMobRenderer;
import com.jason54.unpredictableblocks.client.geo.LuckyRocketRenderer;
import com.jason54.unpredictableblocks.entity.mob.BaristaEntity;
import com.jason54.unpredictableblocks.entity.mob.BodybuilderEntity;
import com.jason54.unpredictableblocks.entity.mob.BotSpeedrunnerEntity;
import com.jason54.unpredictableblocks.entity.mob.ChatInternetEntity;
import com.jason54.unpredictableblocks.entity.mob.CryptoBulleEntity;
import com.jason54.unpredictableblocks.entity.mob.ChefItalienEntity;
import com.jason54.unpredictableblocks.entity.mob.ComteDraculaEntity;
import com.jason54.unpredictableblocks.entity.mob.CoqGauloisEntity;
import com.jason54.unpredictableblocks.entity.mob.DanseurDiscoEntity;
import com.jason54.unpredictableblocks.entity.mob.DouanierEntity;
import com.jason54.unpredictableblocks.entity.mob.EcranBleuEntity;
import com.jason54.unpredictableblocks.entity.mob.GrossisteClandestinEntity;
import com.jason54.unpredictableblocks.entity.mob.HackerFantomeEntity;
import com.jason54.unpredictableblocks.entity.mob.LapinExtirpateurEntity;
import com.jason54.unpredictableblocks.entity.mob.ManifestantEntity;
import com.jason54.unpredictableblocks.entity.mob.MascotteHamburgerEntity;
import com.jason54.unpredictableblocks.entity.mob.MimiqueD20Entity;
import com.jason54.unpredictableblocks.entity.mob.NuageOrageEntity;
import com.jason54.unpredictableblocks.entity.mob.PirateTorrentEntity;
import com.jason54.unpredictableblocks.entity.mob.PopItGeantEntity;
import com.jason54.unpredictableblocks.entity.mob.ReveilCauchemarEntity;
import com.jason54.unpredictableblocks.entity.mob.BaguetteVivanteEntity;
import com.jason54.unpredictableblocks.entity.mob.DossierFiscalEntity;
import com.jason54.unpredictableblocks.entity.mob.AlerteDonEntity;
import com.jason54.unpredictableblocks.entity.mob.EpongeSavonEntity;
import com.jason54.unpredictableblocks.entity.mob.CasqueOrbitalEntity;
import com.jason54.unpredictableblocks.entity.mob.ClapCinemaEntity;
import com.jason54.unpredictableblocks.entity.mob.PyramideOeilEntity;
import com.jason54.unpredictableblocks.entity.mob.CleTuyauEntity;
import com.jason54.unpredictableblocks.entity.mob.BiscuitFortuneEntity;
import com.jason54.unpredictableblocks.entity.mob.TenteCampEntity;
import com.jason54.unpredictableblocks.entity.mob.CubeMemeEntity;
import com.jason54.unpredictableblocks.entity.mob.SliderMultiversEntity;
import com.jason54.unpredictableblocks.entity.mob.StatueVivanteEntity;
import com.jason54.unpredictableblocks.entity.mob.ToiDuFuturEntity;
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
        // NeoForge : .get() trop tôt → NPE "Registry Object not present: …:lucky_block".
        // listen() attend que le BlockEntityType soit réellement enregistré.
        ModBlockEntities.LUCKY_BLOCK.listen(type ->
                BlockEntityRendererRegistry.register(type, LuckyBlockRenderer::new));
        EntityRendererRegistry.register(ModEntities.LUCKY_ROCKET, LuckyRocketRenderer::new);
        EntityRendererRegistry.register(ModEntities.CHEF_ITALIEN, ctx -> new LuckyMobRenderer<>(ctx, ChefItalienEntity.ID, 0.5F));
        EntityRendererRegistry.register(ModEntities.COMTE_DRACULA, ctx -> new LuckyMobRenderer<>(ctx, ComteDraculaEntity.ID, 0.5F));
        EntityRendererRegistry.register(ModEntities.MASCOTTE_HAMBURGER, ctx -> new LuckyMobRenderer<>(ctx, MascotteHamburgerEntity.ID, 0.8F));
        EntityRendererRegistry.register(ModEntities.CHAT_INTERNET, ctx -> new LuckyMobRenderer<>(ctx, ChatInternetEntity.ID, 0.4F));
        EntityRendererRegistry.register(ModEntities.COQ_GAULOIS, ctx -> new LuckyMobRenderer<>(ctx, CoqGauloisEntity.ID, 0.6F));
        EntityRendererRegistry.register(ModEntities.ECRAN_BLEU, ctx -> new LuckyMobRenderer<>(ctx, EcranBleuEntity.ID, 0.6F));
        EntityRendererRegistry.register(ModEntities.TOI_DU_FUTUR, ctx -> new LuckyMobRenderer<>(ctx, ToiDuFuturEntity.ID, 0.5F));
        EntityRendererRegistry.register(ModEntities.DOUANIER, ctx -> new LuckyMobRenderer<>(ctx, DouanierEntity.ID, 0.5F));
        EntityRendererRegistry.register(ModEntities.LAPIN_EXTIRPATEUR, ctx -> new LuckyMobRenderer<>(ctx, LapinExtirpateurEntity.ID, 0.4F));
        EntityRendererRegistry.register(ModEntities.GROSSISTE_CLANDESTIN, ctx -> new LuckyMobRenderer<>(ctx, GrossisteClandestinEntity.ID, 0.5F));
        EntityRendererRegistry.register(ModEntities.STATUE_VIVANTE, ctx -> new LuckyMobRenderer<>(ctx, StatueVivanteEntity.ID, 0.6F));
        EntityRendererRegistry.register(ModEntities.MIMIQUE_D20, ctx -> new LuckyMobRenderer<>(ctx, MimiqueD20Entity.ID, 0.5F));
        EntityRendererRegistry.register(ModEntities.BODYBUILDER, ctx -> new LuckyMobRenderer<>(ctx, BodybuilderEntity.ID, 0.6F));
        EntityRendererRegistry.register(ModEntities.DANSEUR_DISCO, ctx -> new LuckyMobRenderer<>(ctx, DanseurDiscoEntity.ID, 0.5F));
        EntityRendererRegistry.register(ModEntities.HACKER_FANTOME, ctx -> new LuckyMobRenderer<>(ctx, HackerFantomeEntity.ID, 0.5F));
        EntityRendererRegistry.register(ModEntities.BOT_SPEEDRUNNER, ctx -> new LuckyMobRenderer<>(ctx, BotSpeedrunnerEntity.ID, 0.5F));
        EntityRendererRegistry.register(ModEntities.BARISTA, ctx -> new LuckyMobRenderer<>(ctx, BaristaEntity.ID, 0.5F));
        EntityRendererRegistry.register(ModEntities.PIRATE_TORRENT, ctx -> new LuckyMobRenderer<>(ctx, PirateTorrentEntity.ID, 0.5F));
        EntityRendererRegistry.register(ModEntities.MANIFESTANT, ctx -> new LuckyMobRenderer<>(ctx, ManifestantEntity.ID, 0.5F));
        EntityRendererRegistry.register(ModEntities.CRYPTO_BULLE, ctx -> new LuckyMobRenderer<>(ctx, CryptoBulleEntity.ID, 0.6F));
        EntityRendererRegistry.register(ModEntities.NUAGE_ORAGE, ctx -> new LuckyMobRenderer<>(ctx, NuageOrageEntity.ID, 0.7F));
        EntityRendererRegistry.register(ModEntities.SLIDER_MULTIVERS, ctx -> new LuckyMobRenderer<>(ctx, SliderMultiversEntity.ID, 0.5F));
        EntityRendererRegistry.register(ModEntities.POP_IT_GEANT, ctx -> new LuckyMobRenderer<>(ctx, PopItGeantEntity.ID, 0.5F));
        EntityRendererRegistry.register(ModEntities.REVEIL_CAUCHEMAR, ctx -> new LuckyMobRenderer<>(ctx, ReveilCauchemarEntity.ID, 0.5F));
        EntityRendererRegistry.register(ModEntities.BAGUETTE_VIVANTE, ctx -> new LuckyMobRenderer<>(ctx, BaguetteVivanteEntity.ID, 0.5F));
        EntityRendererRegistry.register(ModEntities.DOSSIER_FISCAL, ctx -> new LuckyMobRenderer<>(ctx, DossierFiscalEntity.ID, 0.5F));
        EntityRendererRegistry.register(ModEntities.ALERTE_DON, ctx -> new LuckyMobRenderer<>(ctx, AlerteDonEntity.ID, 0.5F));
        EntityRendererRegistry.register(ModEntities.EPONGE_SAVON, ctx -> new LuckyMobRenderer<>(ctx, EpongeSavonEntity.ID, 0.6F));
        EntityRendererRegistry.register(ModEntities.CASQUE_ORBITAL, ctx -> new LuckyMobRenderer<>(ctx, CasqueOrbitalEntity.ID, 0.6F));
        EntityRendererRegistry.register(ModEntities.CLAP_CINEMA, ctx -> new LuckyMobRenderer<>(ctx, ClapCinemaEntity.ID, 0.5F));
        EntityRendererRegistry.register(ModEntities.PYRAMIDE_OEIL, ctx -> new LuckyMobRenderer<>(ctx, PyramideOeilEntity.ID, 0.6F));
        EntityRendererRegistry.register(ModEntities.CLE_TUYAU, ctx -> new LuckyMobRenderer<>(ctx, CleTuyauEntity.ID, 0.5F));
        EntityRendererRegistry.register(ModEntities.BISCUIT_FORTUNE, ctx -> new LuckyMobRenderer<>(ctx, BiscuitFortuneEntity.ID, 0.4F));
        EntityRendererRegistry.register(ModEntities.TENTE_CAMP, ctx -> new LuckyMobRenderer<>(ctx, TenteCampEntity.ID, 0.7F));
        EntityRendererRegistry.register(ModEntities.CUBE_MEME, ctx -> new LuckyMobRenderer<>(ctx, CubeMemeEntity.ID, 0.6F));

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
            // À gauche de l'inventaire : à droite ça chevauche les effets de potion (NeoForge/vanilla).
            final int btnW = 52;
            final int btnH = 20;
            final boolean creative = screen instanceof CreativeModeInventoryScreen;
            final int guiW = creative ? 195 : 176;
            final int guiH = creative ? 136 : 166;
            final int left = (screen.width - guiW) / 2;
            final int top = (screen.height - guiH) / 2;
            final int x = left - btnW - 4;
            final int y = top + (creative ? 6 : 4);
            access.addRenderableWidget(Button.builder(
                            Component.translatable("screen.unpredictableblocks.quest_journal.open"),
                            b -> NetworkManager.sendToServer(RequestQuestJournalPayload.INSTANCE)
                    )
                    .bounds(x, y, btnW, btnH)
                    .build());
        });
    }
}
