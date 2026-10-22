package com.jason54.unpredictableblocks.gametest;

import com.geckolib.animatable.GeoEntity;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

/**
 * Aligne les mobs du mod face à la caméra (IA coupée) et capture : repos, attaque, capacité spéciale.
 * Captures dans fabric/build/run/clientGameTest/screenshots.
 */
public final class MobsClientGameTest implements FabricClientGameTest {
    private static final List<String> MOBS = List.of(
            "chef_italien", "comte_dracula", "mascotte_hamburger", "chat_internet", "coq_gaulois", "ecran_bleu",
            "toi_du_futur", "douanier", "lapin_extirpateur", "grossiste_clandestin", "statue_vivante", "mimique_d20",
            "bodybuilder", "danseur_disco", "hacker_fantome", "bot_speedrunner", "barista", "pirate_torrent",
            "manifestant", "crypto_bulle", "nuage_orage", "slider_multivers", "pop_it_geant", "reveil_cauchemar",
            "baguette_vivante", "dossier_fiscal", "alerte_don", "eponge_savon", "casque_orbital", "clap_cinema",
            "pyramide_oeil", "cle_tuyau", "biscuit_fortune", "tente_camp", "cube_meme");

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            TestServerContext server = singleplayer.getServer();
            server.runCommand("gamemode creative @a");
            server.runCommand("time set noon");
            server.runCommand("gamerule advance_time false");
            server.runCommand("gamerule spawn_mobs false");
            server.runCommand("fill -16 99 -8 16 99 40 minecraft:smooth_stone");
            server.runCommand("fill -16 100 -8 16 108 40 minecraft:air");
            for (int i = 0; i < MOBS.size(); i++) {
                int row = i / 6;
                int col = i % 6;
                double x = -7.5 + col * 3;
                double z = 3.5 + row * 4;
                String id = MOBS.get(i);
                double y = List.of("ecran_bleu","statue_vivante","hacker_fantome","manifestant","crypto_bulle","nuage_orage","slider_multivers","toi_du_futur","comte_dracula","chef_italien","bot_speedrunner","danseur_disco","alerte_don","casque_orbital","cube_meme","pyramide_oeil").contains(id) ? 101 : 100;
                // Rotation 180 : le mob regarde vers -Z, donc vers la caméra
                server.runCommand("summon unpredictableblocks:" + MOBS.get(i) + " " + x + " " + y + " " + z
                        + " {NoAI:1b,Rotation:[180f,0f]}");
            }
            server.runCommand("tp @a 0.5 100 -6.5 facing 0.5 101 5.5");
            singleplayer.getConnection().waitForChunksRender();
            context.waitTicks(40);
            context.takeScreenshot("mobs_idle");

            trigger(server, "attack");
            context.waitTicks(5);
            context.takeScreenshot("mobs_attack_t5");
            context.waitTicks(5);
            context.takeScreenshot("mobs_attack_t10");

            context.waitTicks(20);
            trigger(server, "special");
            context.waitTicks(6);
            context.takeScreenshot("mobs_special_t6");
            context.waitTicks(10);
            context.takeScreenshot("mobs_special_t16");

            // Gros plan de chaque mob (attendre la fin des anims ponctuelles)
            context.waitTicks(30);
            for (int i = 0; i < MOBS.size(); i++) {
                int row = i / 6;
                int col = i % 6;
                double x = -7.5 + col * 3;
                double z = 3.5 + row * 4;
                server.runCommand("tp @a " + x + " 100 " + (z - 3) + " facing " + x + " 100.8 " + z);
                context.waitTicks(10);
                context.takeScreenshot("mob_" + MOBS.get(i));
            }
        }
    }

    private static void trigger(TestServerContext server, String anim) {
        server.runOnServer(mc -> {
            var level = mc.overworld();
            for (Entity entity : level.getEntities((Entity) null, new AABB(-20, 90, -20, 20, 120, 20), e -> e instanceof GeoEntity)) {
                ((GeoEntity) entity).triggerAnim("action", anim);
            }
        });
    }
}
